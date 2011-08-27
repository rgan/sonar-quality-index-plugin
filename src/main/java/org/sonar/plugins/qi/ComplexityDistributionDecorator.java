/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.qi;

import com.google.common.collect.Lists;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.measures.*;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.resources.Scopes;
import org.sonar.api.utils.KeyValueFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A decorator to propagate the complexity distribution bottom up
 */
public class ComplexityDistributionDecorator implements Decorator {

    @DependedUpon
    public Metric dependedUpon() {
        return QIMetrics.QI_COMPLEX_DISTRIBUTION;
    }

    @DependsUpon
    public List<Metric> aggregateDependsUpon() {
        return Lists.newArrayList(CoreMetrics.COMPLEXITY, CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION);
    }

    public boolean shouldExecuteOnProject(Project project) {
        return Utils.shouldExecuteOnProject(project);
    }

    public void decorate(Resource resource, DecoratorContext context) {
        Measure measure = context.getMeasure(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION);
        if (measure != null) {
            //System.out.println("Functional Complexity distribution:" + measure.getData() + "Resource:" + resource.getLongName() + "Scope:" + resource.getScope());
            context.saveMeasure(new Measure(QIMetrics.QI_COMPLEX_DISTRIBUTION, convertToQIScale(measure.getData())));
            return;
        }
        if (Scopes.isBlockUnit(resource)) {
            double methodComplexity = MeasureUtils.getValue(context.getMeasure(CoreMetrics.COMPLEXITY), 0.0);
            RangeDistributionBuilder distribution = new RangeDistributionBuilder(QIMetrics.QI_COMPLEX_DISTRIBUTION, QIPlugin.COMPLEXITY_BOTTOM_LIMITS);
            distribution.add(methodComplexity);
            context.saveMeasure(distribution.build());
        } else {
            computeAndSaveComplexityDistribution(resource, context, QIPlugin.COMPLEXITY_BOTTOM_LIMITS);
        }
    }

    public String convertToQIScale(String data) {
        // Source mon reports in this scale: 1, 2, 4, 6, 8, 10, 12
        // QI expects 2,10,20,30
        Map<Integer, Integer> distribution = KeyValueFormat.parse(data, new KeyValueFormat.IntegerNumbersPairTransformer());
        StringBuffer result = new StringBuffer();
        result.append("2=" + getValueForRange(distribution, 1, 2));
        result.append(";10=" + getValueForRange(distribution, 3, 10));
        result.append(";20=" + getValueForRange(distribution, 11, 20));
        result.append(";30="+ getValueForRange(distribution, 21, 30));
        return result.toString();
    }

    private int getValueForRange(Map<Integer, Integer> distribution, int start, int end) {
        int result = valueFromDistribution(distribution, start);
        for(int i=start+1; i <= end; i++) {
            result += valueFromDistribution(distribution, i);
        }
        return result;
    }


    private Integer valueFromDistribution(Map<Integer, Integer> distribution, int key) {
        return distribution.get(key) == null ? 0 : distribution.get(key);
    }

    /**
     * Computes and saves the complexity distribution at the resource level.
     * The distribution is persisted in DB only at project level to make sure it cand be used at "higher" level
     *
     * @param resource     the resource
     * @param context      the context
     * @param bottomLimits the bottom limits of complexity ranges
     */
    protected void computeAndSaveComplexityDistribution(Resource resource, DecoratorContext context, Number[] bottomLimits) {
        Measure measure = computeComplexityDistribution(context, bottomLimits);
        if (!ResourceUtils.isProject(resource)) {
            measure.setPersistenceMode(PersistenceMode.MEMORY);
        }
        context.saveMeasure(measure);
    }

    /**
     * Computes the complexity distribution by adding up the children distribution
     *
     * @param context      the context
     * @param bottomLimits the bottom limits of complexity ranges
     * @return the measure
     */
    protected Measure computeComplexityDistribution(DecoratorContext context, Number[] bottomLimits) {
        RangeDistributionBuilder builder = new RangeDistributionBuilder(QIMetrics.QI_COMPLEX_DISTRIBUTION, bottomLimits);
        for (Measure childMeasure : context.getChildrenMeasures(QIMetrics.QI_COMPLEX_DISTRIBUTION)) {
            builder.add(childMeasure);
        }
        return builder.build();
    }
}
