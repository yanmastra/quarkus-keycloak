package io.yanmastra.quarkus.microservices.common.messaging;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.StringReader;
import java.util.Map;

public class SecureMsgSerializer implements Serializer<MessageQuote> {
    private final StringSerializer stringSerializer;

    public SecureMsgSerializer() {
        this.stringSerializer = new StringSerializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        stringSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, MessageQuote tMessageQuote) {
        Map<String, Object> claimsMap = tMessageQuote.getPrincipal().getClaims().getClaimsMap();
        JsonObjectBuilder claims = Json.createObjectBuilder();
        for (String key: claimsMap.keySet()) {
            if (claimsMap.get(key) instanceof JsonValue jsonValue)
                claims.add(key, jsonValue);
            else if (claimsMap.get(key) instanceof JsonObject jObj) {
                claims.add(key, Json.createObjectBuilder(jObj));
            }
        }

        JsonObject json = Json.createObjectBuilder()
                .add("claims", claims)
                .add("cred",
                        Json.createObjectBuilder()
                        .add("token", "[hidden]")
                        .add("type", tMessageQuote.getPrincipal().getCredential().getType())
                )
                .add("data", Json.createReader(new StringReader(tMessageQuote.getDataJson())).readValue())
                .build();
        return stringSerializer.serialize(s, json.toString());
    }

    @Override
    public void close() {
        stringSerializer.close();
    }
}
