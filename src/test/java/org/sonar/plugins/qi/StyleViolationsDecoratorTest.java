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
import com.google.common.collect.Multiset;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StyleViolationsDecoratorTest {


    private StyleViolationsDecorator decorator;
    private DecoratorContext context;
    private Configuration configuration;

    @Test
    public void testValidLines() {
        decorator = new StyleViolationsDecorator(null);
        context = mock(DecoratorContext.class);

        when(context.getMeasure(CoreMetrics.DUPLICATED_LINES)).thenReturn(new Measure(CoreMetrics.DUPLICATED_LINES, 233.0));
        when(context.getMeasure(CoreMetrics.NCLOC)).thenReturn(new Measure(CoreMetrics.NCLOC, 1344.0));

        assertThat(decorator.getValidLines(context), is(11110.0));
    }

    @Test
    public void testCountViolationsByPriority() {
        Configuration configuration = mock(Configuration.class);
        decorator = new StyleViolationsDecorator(configuration);
        context = mock(DecoratorContext.class);
        when(configuration.getString(anyString(), anyString())).
                thenReturn("MAJOR=3;BLOCKER=17;INFO=6");

        List<Violation> violations = Lists.newArrayList(
                new Violation(new Rule(CodingViolationsDecorator.GENDARME_PLUGIN, "a")).setPriority(RulePriority.BLOCKER),
                new Violation(new Rule(StyleViolationsDecorator.STYLECOP_PLUGIN, "c")).setPriority(RulePriority.BLOCKER)
        );
        when(context.getViolations()).
                thenReturn(violations);
        Multiset<RulePriority> set = decorator.countViolationsByPriority(context);
        assertThat(set.count(RulePriority.BLOCKER), is(1));
    }
}
