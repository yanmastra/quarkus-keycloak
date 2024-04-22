package io.yanmastra.commonClass.messaging;

import io.quarkus.security.credential.TokenCredential;
import io.yanmastra.authorization.security.UserPrincipal;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jose4j.jwt.JwtClaims;

import java.io.StringReader;
import java.util.Map;

public class SecureMsgDeserializer implements Deserializer<MessageQuote> {
    private final StringDeserializer stringDeserializer;

    public SecureMsgDeserializer() {
        this.stringDeserializer = new StringDeserializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        stringDeserializer.configure(configs, isKey);
    }

    @Override
    public MessageQuote deserialize(String s, byte[] bytes) {
        String message = stringDeserializer.deserialize(s, bytes);
        JsonObject msgObj = Json.createReader(new StringReader(message)).readObject();

        JwtClaims claims = new JwtClaims();
        for (Map.Entry<String, JsonValue> entry : msgObj.getJsonObject("claims").entrySet()) {
            claims.setClaim(entry.getKey(), entry.getValue());
        }

        TokenCredential credential = new TokenCredential(
                msgObj.getJsonObject("cred").getString("token"),
                msgObj.getJsonObject("cred").getString("type")
        );

        String data = msgObj.getJsonObject("data").toString();
        return new MessageQuote(data, new UserPrincipal(claims, credential));
    }
}
