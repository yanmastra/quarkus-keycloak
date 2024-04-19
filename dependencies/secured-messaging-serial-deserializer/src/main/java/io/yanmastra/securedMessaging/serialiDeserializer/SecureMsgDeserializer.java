package io.yanmastra.securedMessaging.serialiDeserializer;

import io.quarkus.security.credential.TokenCredential;
import io.vertx.core.json.JsonObject;
import io.yanmastra.authorization.security.UserPrincipal;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jose4j.jwt.JwtClaims;

import java.util.Iterator;
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
        JsonObject msgObj = new JsonObject(message);

        JwtClaims claims = new JwtClaims();
        Iterator<Map.Entry<String, Object>> claimIterator = msgObj.getJsonObject("claims").stream().iterator();
        while (claimIterator.hasNext()) {
            Map.Entry<String, Object> entry = claimIterator.next();
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
