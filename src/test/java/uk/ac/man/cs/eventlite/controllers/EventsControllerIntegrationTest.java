package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;

import uk.ac.man.cs.eventlite.EventLite;

import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.test.web.reactive.server.EntityExchangeResult;



@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");	
	private static String CSRF_HEADER = "X-CSRF-TOKEN";
	private static String SESSION_KEY = "JSESSIONID";

	@LocalServerPort
	private int port;

	private WebTestClient client;

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});

	}

	@Test
	public void searchForEventWithExactName() throws Exception {
		client.get().uri("/events/search?keyword=COMP23412 Showcase, group F").accept(MediaType.TEXT_HTML).exchange()
				.expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("COMP23412 Showcase, group F"));
				});
	}
	
	@Test
	public void getUpdateEventNotFound() {
		client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/updateEvent/99")
				.accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
						assertThat(result.getResponseBody(), containsString("99"));
		});

	}

	@Test
	public void searchForEventWithSubstringOfName() throws Exception {
		client.get().uri("/events/search?keyword=COMP").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("COMP23412 Showcase, group F"));
				});
	}

	@Test
	public void searchForNonExistingEvent() throws Exception {
		client.get().uri("/events/search?keyword=dave").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody(), not(containsString("COMP23412 Showcase, group F")));
				});
	}
	
	@Test
	@DirtiesContext
	public void deleteEventWithUser() {
		int currentRows = countRowsInTable("events");
		String[] tokens = login();

		// The session ID cookie holds our login credentials.
		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
		client.delete().uri("/events/2").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isFound().expectHeader()
				.value("Location", endsWith("/events"));

		// Check that one row is removed from the database.
		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
	}

//	@Test
//	public void deleteEventNotFound() {
//		int currentRows = countRowsInTable("events");
//		String[] tokens = login();
//
//		// The session ID cookie holds our login credentials.
//		// And for a DELETE we have no body, so we pass the CSRF token in the headers.
//		client.delete().uri("/events/99").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
//				.cookie(SESSION_KEY, tokens[1]).exchange().expectStatus().isNotFound();
//
//		// Check nothing is removed from the database.
//		assertThat(currentRows, equalTo(countRowsInTable("events")));
//	}

	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/sign-in").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}
	
	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called; might as well assert something as well...
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
	
	@Disabled
	@Test
	public void orderRetainedAfterSearch() {		
		client.get().uri("/events/search?keyword=group+B").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
		.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class)
		.consumeWith(result -> {
			System.out.println(result.getResponseBody());
			assertThat(result.getResponseBody(), true );
		});
	}
	
}
