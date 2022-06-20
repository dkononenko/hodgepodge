package com.hrsinternational.tng4;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

public class AvroConverter extends AbstractBehavior<AvroConverter.Command<?>> {
    public interface Command<T> {
        ActorRef<StatusReply<T>> replyTo();
    }

    public record Encode(SpecificRecordBase srb, ActorRef<StatusReply<byte[]>> replyTo) implements Command<byte[]> {
    }

    public record Decode(byte[] message,
                         ActorRef<StatusReply<SpecificRecordBase>> replyTo) implements Command<SpecificRecordBase> {
    }

    public static Behavior<AvroConverter.Command<?>> setup() {
        return Behaviors.setup(AvroConverter::new);
    }

    @Override
    public Receive<AvroConverter.Command<?>> createReceive() {
        return newReceiveBuilder()
                .onMessage(Encode.class, command -> {
                    try {
                        try (var baos = new ByteArrayOutputStream()) {
                            var writer = new SpecificDatumWriter<>(schema);
                            var encoder = EncoderFactory.get().binaryEncoder(baos, null);
                            writer.write(command.srb(), encoder);
                            encoder.flush();
                            command.replyTo().tell(StatusReply.success(baos.toByteArray()));
                        }
                    } catch (Exception e) {
                        getContext().getLog().error("Can't encode", e);
                        command.replyTo().tell(StatusReply.error(e.getMessage()));
                    }

                    return Behaviors.same();
                })
                .onMessage(Decode.class, command -> {
                    try {
                        var reader = new SpecificDatumReader<>(schema);
                        var encoder = DecoderFactory.get().binaryDecoder(command.message(), null);
                        command.replyTo().tell(StatusReply.success((SpecificRecordBase) reader.read(null, encoder)));
                    } catch (Exception e) {
                        getContext().getLog().error("Can't decode", e);
                        command.replyTo().tell(StatusReply.error(e.getMessage()));
                    }

                    return Behaviors.same();
                })
                .build();
    }

    private AvroConverter(ActorContext<AvroConverter.Command<?>> context) throws Exception {
        super(context);

        var url = Objects.requireNonNull(getClass().getClassLoader().getResource("avro/organization.avsc"));
        schema = new Schema.Parser().parse(new File(url.toURI()));
    }

    private final Schema schema;
}
