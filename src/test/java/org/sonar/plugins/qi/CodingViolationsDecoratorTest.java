package org.sonar.plugins.qi;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.Violation;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CodingViolationsDecoratorTest {
    private AbstractViolationsDecorator decorator;
    private DecoratorContext context;
    private Configuration configuration;

    @Before
    public void init() {
        context = mock(DecoratorContext.class);
        configuration = mock(Configuration.class);
        decorator = new CodingViolationsDecorator(configuration);
    }

    @Test
    public void testDependsUpon() {
        assertThat(decorator.dependsUpon().size(), is(1));
    }


    @Test
    public void testCountViolationsByPriority() {
        createMultiSetViolationsForDotNet();
        Multiset<RulePriority> set = decorator.countViolationsByPriority(context);
        assertThat(set.count(RulePriority.BLOCKER), is(2));
        assertThat(set.count(RulePriority.CRITICAL), is(0));
        assertThat(set.count(RulePriority.MAJOR), is(1));
        assertThat(set.count(RulePriority.INFO), is(1));
    }

    private void createMultiSetViolationsForDotNet() {
        List<Violation> violations = Lists.newArrayList(
                new Violation(new Rule(CodingViolationsDecorator.GENDARME_PLUGIN, "a")).setPriority(RulePriority.BLOCKER),
                new Violation(new Rule(CodingViolationsDecorator.FXCOP_PLUGIN, "b")).setPriority(RulePriority.BLOCKER),
                new Violation(new Rule(CoreProperties.CHECKSTYLE_PLUGIN, "c")).setPriority(RulePriority.BLOCKER),
                new Violation(new Rule("joe", "bloch")).setPriority(RulePriority.BLOCKER),
                new Violation(new Rule(CodingViolationsDecorator.GENDARME_PLUGIN, "e")).setPriority(RulePriority.MAJOR),
                new Violation(new Rule(CodingViolationsDecorator.GENDARME_PLUGIN, "hic")).setPriority(RulePriority.INFO)
        );
        when(context.getViolations()).
                thenReturn(violations);
    }

}
