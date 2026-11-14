package com.atsuishio.superbwarfare.data;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.Identifier;

import java.io.IOException;

public class ResourceLocationAdapter extends TypeAdapter<Identifier> {

    @Override
    public void write(JsonWriter out, Identifier value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.toString());
    }

    @Override
    public Identifier read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        if (in.peek() != JsonToken.STRING) {
            throw new IllegalStateException("excepted Identifier to be String but was " + in.peek());
        }

        return Identifier.tryParse(in.nextString());
    }
}
