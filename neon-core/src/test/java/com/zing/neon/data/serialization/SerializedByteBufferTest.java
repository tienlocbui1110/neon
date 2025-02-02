package com.zing.neon.data.serialization;

import java.util.Random;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SerializedByteBufferTest {

    private Random random = new Random();
    private byte[] bytes;
    private SerializedByteBufferOutput gWriter;
    private SerializedByteBufferInput gReader;

    @Test
    public void serialize() {
        SerializedByteBufferOutput output = new SerializedByteBufferOutput();
        writeBinary(output);
        SerializedByteBufferInput input = new SerializedByteBufferInput(output.toByteArray());
        checkValid(input);
    }

    @Test
    public void maxCapacity() {
        // Valid
        allocateCapacity(2);
        gWriter.writeInt16(255);
        recreateReader();
        assertEquals(255, gReader.readInt16());
        // Invalid
        try {
            allocateCapacity(2);
            // Need 2bytes to store
            gWriter.writeInt16(256);
            fail();
        } catch (RuntimeException e) {
            assertThat(e.toString(), CoreMatchers.containsString("write byte error"));
        }
    }

    @Test
    public void testInt16() {
        allocateCapacity(3, 1);
        gWriter.writeInt16(1000);
        recreateReader();
        assertEquals(1000, gReader.readInt16());
        allocateCapacity(3);
        gWriter.writeInt16(500);
        recreateReader();
        assertEquals(500, gReader.readInt16());
    }

    @Test
    public void testInt32() {
        allocateCapacity(5, 1);
        gWriter.writeInt32(5000000);
        recreateReader();
        assertEquals(5000000, gReader.readInt32());
        allocateCapacity(5, 3);
        gWriter.writeInt32(22);
        recreateReader();
        assertEquals(22, gReader.readInt32());
        allocateCapacity(5);
        gWriter.writeInt32(99);
        recreateReader();
        assertEquals(99, gReader.readInt32());
    }

    @Test
    public void testInt64() {
        allocateCapacity(9, 1);
        gWriter.writeInt64(5000000);
        recreateReader();
        assertEquals(5000000, gReader.readInt64());
        allocateCapacity(9, 7);
        gWriter.writeInt64(22);
        recreateReader();
        assertEquals(22, gReader.readInt64());
        allocateCapacity(9);
        gWriter.writeInt64(99);
        recreateReader();
        assertEquals(99, gReader.readInt64());
    }

    @Test
    public void testDouble() {
        allocateCapacity(9, 1);
        gWriter.writeDouble(22.2558);
        recreateReader();
        assertEquals(22.2558, gReader.readDouble(), 0.0000001);
        allocateCapacity(9, 7);
        gWriter.writeDouble(33.33333);
        recreateReader();
        assertEquals(33.33333, gReader.readDouble(), 0.0000001);
        allocateCapacity(9);
        gWriter.writeDouble(99);
        recreateReader();
        assertEquals(99, gReader.readDouble(), 0.0000001);
    }

    @Test
    public void testBoolean() {
        allocateCapacity(1, 1);
        gWriter.writeBool(false);
        recreateReader();
        assertFalse(gReader.readBool());
        allocateCapacity(1, 0);
        gWriter.writeBool(true);
        recreateReader();
        assertTrue(gReader.readBool());
    }

    @Test
    public void testBytes() {
        bytes = new byte[10];
        random.nextBytes(bytes);
        allocateCapacity(1024, 10);
        gWriter.writeBytes(bytes);
        recreateReader();
        assertArrayEquals(bytes, gReader.readByteArray());
        allocateCapacity(14, 1);
        gWriter.writeBytes(bytes);
        recreateReader();
        assertArrayEquals(bytes, gReader.readByteArray());
    }

    @Test
    public void testString() {
        String sTest = "com.zing.zalo";
        allocateCapacity(1024, sTest.length());
        gWriter.writeString(sTest);
        recreateReader();
        assertEquals(sTest, gReader.readString());
        allocateCapacity(sTest.length() + 4, sTest.length() - 1);
        gWriter.writeString(sTest);
        recreateReader();
        assertEquals(sTest, gReader.readString());
    }

    private void allocateCapacity(int capacity) {
        gWriter = new SerializedByteBufferOutput(capacity);
    }

    private void allocateCapacity(int capacity, int defaultSize) {
        gWriter = new SerializedByteBufferOutput(capacity, defaultSize);
    }

    private void allocateCapacity() {
        gWriter = new SerializedByteBufferOutput();
    }

    private void recreateReader() {
        gReader = new SerializedByteBufferInput(gWriter.toByteArray());
    }

    private void writeBinary(SerializedByteBufferOutput output) {
        output.writeInt32(50);
        output.writeInt64(999999999999999L);
        output.writeBool(false);
        output.writeByte((byte) 100);
        bytes = new byte[100];
        random.nextBytes(bytes);
        output.writeBytes(bytes);
        output.writeBytes(bytes, 10, 50);
        output.writeInt16(999999);
    }

    private void checkValid(SerializedByteBufferInput input) {
        assertEquals(50, input.readInt32());
        assertEquals(999999999999999L, input.readInt64());
        assertFalse(input.readBool());
        assertEquals(100, input.readByte());
        assertArrayEquals(bytes, input.readByteArray());
        byte[] offsetCheck = new byte[50];
        System.arraycopy(bytes, 10, offsetCheck, 0, 50);
        assertArrayEquals(offsetCheck, input.readByteArray());
        assertEquals(16959, input.readInt16());
    }
}
