package com.chapter1.blueprint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.Page;

import java.io.IOException;

public class PageSerializer extends StdSerializer<Page<?>> {

    @SuppressWarnings("unchecked")
    public PageSerializer() {
        super((Class<Page<?>>) (Class<?>) Page.class);
    }

    @Override
    public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("content", page.getContent());
        gen.writeNumberField("totalPages", page.getTotalPages());
        gen.writeNumberField("totalElements", page.getTotalElements());
        gen.writeNumberField("number", page.getNumber());
        gen.writeNumberField("size", page.getSize());
        gen.writeBooleanField("first", page.isFirst());
        gen.writeBooleanField("last", page.isLast());
        gen.writeEndObject();
    }
}
