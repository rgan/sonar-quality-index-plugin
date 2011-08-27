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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.commons.configuration.Configuration;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;

import java.util.List;

public class CodingViolationsDecorator extends AbstractViolationsDecorator {

    public static final String GENDARME_PLUGIN = "gendarme";
    public static final String FXCOP_PLUGIN = "fxcop";

    /**
     * Creates a CodingViolationsDecorator, i.e. implements an AbstractViolationsDecorator
     * to decorate the Coding axis of the QI
     *
     * @param configuration the configuration
     */
    public CodingViolationsDecorator(Configuration configuration) {
        super(configuration, QIMetrics.QI_CODING_VIOLATIONS,
                QIPlugin.QI_CODING_AXIS_WEIGHT, QIPlugin.QI_CODING_AXIS_WEIGHT_DEFAULT);
    }

    /**
     * @return the coding axis weight config key
     */
    @Override
    public String getConfigurationKey() {
        return QIPlugin.QI_CODING_PRIORITY_WEIGHTS;
    }

    /**
     * @return the coding axis default weight config key
     */
    @Override
    public String getDefaultConfigurationKey() {
        return QIPlugin.QI_CODING_PRIORITY_WEIGHTS_DEFAULT;
    }

    /**
     * @return the metric to store the coding weighted violations
     */
    public Metric getWeightedViolationMetricKey() {
        return QIMetrics.QI_CODING_WEIGHTED_VIOLATIONS;
    }

    @Override
    public boolean includeRule(Rule rule) {
        String pluginName = rule.getPluginName();
        return pluginName.equalsIgnoreCase(GENDARME_PLUGIN) ||
                pluginName.equalsIgnoreCase(FXCOP_PLUGIN) || pluginName.equals(CoreProperties.PMD_PLUGIN);
    }

}
