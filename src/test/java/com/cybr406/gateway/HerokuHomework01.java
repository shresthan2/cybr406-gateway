package com.cybr406.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This homework will verify your Heroku deployment by making requests to your gateway project. Your gateway
 * should route the requests to the correct locations in Heroku.
 *
 * Before you run this test, you should deploy all three applications to Heroku (gateway, account, and post).
 * Please name your project using your UNK email. For example:
 *
 * 		lowryrs-blog (gateway)
 * 	    lowryrs-account (account)
 * 	    lowryrs-post (post)
 *
 * Once your deployments are complete, they should reside at URLs that resemble the following:
 *
 *     https://lowryrs-blog.herokuapp.com (gateway)
 *     https://lowryrs-account.herokuapp.com (account)
 *     https://lowryrs-post.herokuapp.com (post)
 *
 * To prepare for the deployments, update the URLs in the following files to match your anticipated URLS:
 *
 *     cybr406-gateway/src/main/resources/application-heroku.properties
 *          account.url -> update to match the url of your deployed account project
 *          post.url    -> update to match the url of your deployed post project
 *     cybr406-post/src/main/resources/application-heroku.properties
 *     		account.url -> update to match the url of your deployed account project
 *
 * Video Links:
 *     Heroku Setup: https://use.vg/PbglKQ
 */
@SpringBootTest
class HerokuHomework01 {

	// You must update baseUrl to match your deployed gateway project URL.
	// It should resemble the value that is there currently.
	private static final String baseUrl = "https://cybr406-blog.herokuapp.com";

	private WebClient webClient = WebClient.create(baseUrl);

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void problem_01_getProfiles() {
		ClientResponse response = webClient.get()
				.uri("/profiles")
				.exchange()
				.block();
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());
	}

	@Test
	void problem_02_getPosts() {
		ClientResponse response = webClient.get()
				.uri("/posts")
				.exchange()
				.block();
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());
	}

	@Test
	void problem_03_getComments() {
		ClientResponse response = webClient.get()
				.uri("/comments")
				.exchange()
				.block();
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());
	}

	@Test
	void problem_04_signUp() {
		createRandomUser();
	}

	@Test
	void problem_05_createPost() throws Exception {
		String[] credentials = createRandomUser();
		ObjectNode content = createRandomPost(credentials);

		URI uri = URI.create(content.get("_links").get("self").get("href").asText());

		ClientResponse response = webClient.get()
				.uri(uri.getPath())
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());
	}

	@Test
	void problem_06_createComment() throws Exception {
		String[] credentials = createRandomUser();

		ObjectNode postContent = createRandomPost(credentials);
		URI postUri = URI.create(postContent.get("_links").get("self").get("href").asText());

		createRandomComment(postUri, credentials);
		URI commentUri = URI.create(postContent.get("_links").get("self").get("href").asText());

		ClientResponse response = webClient.get()
				.uri(commentUri.getPath())
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());
	}

	@Test
	void problem_07_postPermissions() throws Exception {
		String[] user_a = createRandomUser();
		String[] user_b = createRandomUser();

		ObjectNode content = createRandomPost(user_a);
		URI uri = URI.create(content.get("_links").get("self").get("href").asText());

		Map<String, String> patch = new HashMap<>();
		patch.put("text", "Edited by user_a");

		ClientResponse response = webClient.patch()
				.uri(uri.getPath())
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(user_a[0], user_a[1]))
				.bodyValue(patch)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());

		patch = new HashMap<>();
		patch.put("text", "Edited by user_b");

		response = webClient.patch()
				.uri(uri.getPath())
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(user_b[0], user_b[1]))
				.bodyValue(patch)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
	}

	@Test
	void problem_08_commentPermissions() throws Exception {
		String[] user_a = createRandomUser();
		String[] user_b = createRandomUser();

		ObjectNode postContent = createRandomPost(user_a);
		URI postUri = URI.create(postContent.get("_links").get("self").get("href").asText());

		createRandomComment(postUri, user_a);
		URI commentUri = URI.create(postContent.get("_links").get("self").get("href").asText());

		Map<String, String> patch = new HashMap<>();
		patch.put("text", "Edited by user_a");

		ClientResponse response = webClient.patch()
				.uri(commentUri.getPath())
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(user_a[0], user_a[1]))
				.bodyValue(patch)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.statusCode());

		patch = new HashMap<>();
		patch.put("text", "Edited by user_b");

		response = webClient.patch()
				.uri(commentUri.getPath())
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(user_b[0], user_b[1]))
				.bodyValue(patch)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
	}

	String[] createRandomUser() {
		String[] credentials = new String[]{
				UUID.randomUUID().toString().substring(0, 8),
				UUID.randomUUID().toString().substring(0, 8)
		};

		Map<String, String> signup = new HashMap<>();
		signup.put("username", credentials[0]);
		signup.put("password", credentials[1]);
		signup.put("firstName", UUID.randomUUID().toString().substring(0, 8));
		signup.put("lastName", UUID.randomUUID().toString().substring(0, 8));
		signup.put("info", "I'm a randomly generated test user.");

		ClientResponse response = webClient.post()
				.uri("/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(signup)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.statusCode());

		return credentials;
	}

	ObjectNode createRandomPost(String[] credentials) throws Exception {
		Map<String, String> post = new HashMap<>();
		post.put("text", "This is an automated test post.");

		ClientResponse response = webClient.post()
				.uri("/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(credentials[0], credentials[1]))
				.bodyValue(post)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.statusCode());

		String body = response.bodyToMono(String.class).block();
		Objects.requireNonNull(body);
		System.out.println(body);
		return objectMapper.readValue(body, ObjectNode.class);
	}

	ObjectNode createRandomComment(URI postURI, String[] credentials) throws Exception {
		Map<String, String> comment = new HashMap<>();
		comment.put("post", postURI.toString());
		comment.put("text", "This is an automated test comment.");

		ClientResponse response = webClient.post()
				.uri("/comments")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(h -> h.setBasicAuth(credentials[0], credentials[1]))
				.bodyValue(comment)
				.exchange()
				.block();

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.statusCode());

		String body = response.bodyToMono(String.class).block();
		Objects.requireNonNull(body);
		System.out.println(body);
		return objectMapper.readValue(body, ObjectNode.class);
	}

}
