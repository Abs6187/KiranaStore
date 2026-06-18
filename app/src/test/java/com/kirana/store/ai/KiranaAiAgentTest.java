package com.kirana.store.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.junit.Test;

/**
 * Unit tests for the pure JSON-parsing logic of {@link KiranaAiAgent}.
 * <p>
 * These exercise {@link KiranaAiAgent#parseResponseJson(String, String)} only;
 * the live Firebase SDK call is intentionally not covered (requires a network
 * round-trip + valid API key).
 * <p>
 * Lives in the same package ({@code com.kirana.store.ai}) so it can call the
 * package-private method. Prices are in Indian Rupees (₹).
 */
public class KiranaAiAgentTest {

    private static final String TRANSCRIPT = "mustard oil ka price 175 rupee karo";

    @Test
    public void parsesUpdatePriceCommand() throws JSONException {
        String json = "{\"action\":\"update_price\",\"product\":\"Mustard Oil\","
            + "\"price\":175,\"unit\":null,"
            + "\"acknowledgement\":\"Updated Mustard Oil to ₹175.\"}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, TRANSCRIPT);

        assertEquals("update_price", cmd.action);
        assertEquals("Mustard Oil", cmd.product);
        assertEquals(175.0, cmd.price, 0.001);
        assertEquals("", cmd.unit);          // null in JSON → default ""
        assertEquals("Updated Mustard Oil to ₹175.", cmd.acknowledgement);
        assertEquals(TRANSCRIPT, cmd.rawTranscript);
    }

    @Test
    public void parsesAddProductCommandWithUnit() throws JSONException {
        String json = "{\"action\":\"add_product\",\"product\":\"Atta 5kg\","
            + "\"price\":280,\"unit\":\"5kg\","
            + "\"acknowledgement\":\"Added Atta 5kg at ₹280.\"}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, "atta 5 kg 280 rupees add karo");

        assertEquals("add_product", cmd.action);
        assertEquals("Atta 5kg", cmd.product);
        assertEquals(280.0, cmd.price, 0.001);
        assertEquals("5kg", cmd.unit);
        assertEquals("Added Atta 5kg at ₹280.", cmd.acknowledgement);
    }

    @Test
    public void parsesQueryPriceCommandWithNullPrice() throws JSONException {
        String json = "{\"action\":\"query_price\",\"product\":\"Basmati Rice\","
            + "\"price\":null,\"unit\":null}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, "basmati rice ki price kya hai");

        assertEquals("query_price", cmd.action);
        assertEquals("Basmati Rice", cmd.product);
        assertEquals(-1.0, cmd.price, 0.001); // null price resolved to -1
    }

    @Test
    public void parsesUnknownAction() throws JSONException {
        String json = "{\"action\":\"unknown\",\"product\":\"\",\"price\":null,\"unit\":null}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, "...");

        assertEquals("unknown", cmd.action);
    }

    @Test
    public void missingActionField_defaultsToUnknown() throws JSONException {
        String json = "{\"product\":\"Sugar\",\"price\":48}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, "sugar 48");

        assertEquals("unknown", cmd.action);
        assertEquals(48.0, cmd.price, 0.001);
    }

    @Test
    public void missingAcknowledgement_usesDefaultFallback() throws JSONException {
        String json = "{\"action\":\"update_price\",\"product\":\"Sugar\",\"price\":48}";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(json, "sugar");

        assertTrue("fallback acknowledgement should mention the product",
            cmd.acknowledgement.contains("Sugar"));
    }

    @Test
    public void stripsMarkdownJsonFencing() throws JSONException {
        String fenced = "```json\n"
            + "{\"action\":\"update_price\",\"product\":\"Toor Dal\",\"price\":140}"
            + "\n```";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(fenced, TRANSCRIPT);

        assertEquals("update_price", cmd.action);
        assertEquals("Toor Dal", cmd.product);
        assertEquals(140.0, cmd.price, 0.001);
    }

    @Test
    public void stripsBareFencing() throws JSONException {
        String fenced = "```\n"
            + "{\"action\":\"update_price\",\"product\":\"Toor Dal\",\"price\":140}"
            + "\n```";

        KiranaAiAgent.ParsedCommand cmd =
            KiranaAiAgent.parseResponseJson(fenced, TRANSCRIPT);

        assertEquals("Toor Dal", cmd.product);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInput_throws() throws JSONException {
        KiranaAiAgent.parseResponseJson(null, TRANSCRIPT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankInput_throws() throws JSONException {
        KiranaAiAgent.parseResponseJson("   ", TRANSCRIPT);
    }

    @Test(expected = JSONException.class)
    public void invalidJson_throws() throws JSONException {
        KiranaAiAgent.parseResponseJson("this is not json at all", TRANSCRIPT);
    }
}
