package io.yanmastra.commonClass.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import io.yanmastra.authorization.utils.JsonUtils;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

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
        String json = JsonUtils.toJson(Map.of(
                "claims", tMessageQuote.getPrincipal().getClaims().getClaimsMap(),
                "cred", Map.of(
                        "token", "[hidden]",
                        "type", tMessageQuote.getPrincipal().getCredential().getType()
                ),
                "data", tMessageQuote.getDataObject(new TypeReference<Map<String, Object>>() {})
        ));
        return stringSerializer.serialize(s, json);
    }

    @Override
    public void close() {
        stringSerializer.close();
    }
}
