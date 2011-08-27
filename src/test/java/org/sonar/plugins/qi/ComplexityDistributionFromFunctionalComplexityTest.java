package org.sonar.plugins.qi;

import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComplexityDistributionFromFunctionalComplexityTest {

    @Test
    public void shouldConvertToQIScale() {
        ComplexityDistributionDecorator decorator = new ComplexityDistributionDecorator();
        String result = decorator.convertToQIScale("1=0;2=1;4=1;6=2;8=2;10=0;12=0");
        assertThat(result, is("2=1;10=5;20=0;30=0"));
    }

    @Test
    public void shouldComputeDistributionFromFunctionalComplexityDistributionMetric() {
        DecoratorContext context = mock(DecoratorContext.class);
        Measure measure = new Measure(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, "1=0;2=1;4=1;6=2;8=2;10=0;12=0");
        when(context.getMeasure(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION)).thenReturn(measure);

        ComplexityDistributionDecorator decorator = new ComplexityDistributionDecorator();
        decorator.decorate(null, context);
        verify(context).saveMeasure(isA(Measure.class));
    }


}
