package br.com.caelum.vraptor.serialization;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.vraptor.http.FormatResolver;
import br.com.caelum.vraptor.view.PageResult;

public class DefaultRepresentationResultTest {

	@Mock private FormatResolver formatResolver;
	@Mock private Serialization serialization;
	@Mock private PageResult result;

	private RepresentationResult representation;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		representation = new DefaultRepresentationResult(formatResolver, result, Arrays.asList(serialization));
	}

	@Test
	public void whenThereIsNoFormatGivenShouldForwardToDefaultPage() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn(null);

		Serializer serializer = representation.from(new Object());

		assertThat(serializer, is(instanceOf(NullSerializer.class)));
		verify(result).forward();
	}
	@Test
	public void whenThereIsAFormatGivenShouldUseCorrectSerializer() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn("xml");

		when(serialization.accepts("xml")).thenReturn(true);
		Object object = new Object();

		representation.from(object);

		verify(serialization).from(object);
	}
	@Test
	public void whenSerializationDontAcceptsFormatItShouldntBeUsed() throws Exception {
		when(formatResolver.getAcceptFormat()).thenReturn("xml");

		when(serialization.accepts("xml")).thenReturn(false);
		Object object = new Object();

		representation.from(object);

		verify(serialization, never()).from(object);
	}
}
