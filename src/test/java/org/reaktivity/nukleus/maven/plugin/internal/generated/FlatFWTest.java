/**
 * Copyright 2016-2019 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.nukleus.maven.plugin.internal.generated;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;
import org.reaktivity.reaktor.internal.test.types.StringFW;
import org.reaktivity.reaktor.internal.test.types.inner.FlatFW;

public class FlatFWTest
{
    private final MutableDirectBuffer buffer = new UnsafeBuffer(allocateDirect(100))
    {
        {
            // Make sure the code is not secretly relying upon memory being initialized to 0
            setMemory(0, capacity(), (byte) 0xab);
        }
    };
    private final MutableDirectBuffer expected = new UnsafeBuffer(allocateDirect(100))
    {
        {
            // Make sure the code is not secretly relying upon memory being initialized to 0
            setMemory(0, capacity(), (byte) 0xab);
        }
    };
    private final FlatFW.Builder flatRW = new FlatFW.Builder();
    private final FlatFW flatRO = new FlatFW();
    private final StringFW.Builder stringRW = new StringFW.Builder();
    private final MutableDirectBuffer valueBuffer = new UnsafeBuffer(allocateDirect(100));

    @Test
    public void shouldProvideTypeId() throws Exception
    {
        int limit = flatRW.wrap(buffer, 0, 100)
                .fixed1(10)
                .string1("value1")
                .string2("value2")
                .build()
                .limit();
        flatRO.wrap(buffer,  0,  limit);
        assertEquals(0x10000001, FlatFW.TYPE_ID);
        assertEquals(0x10000001, flatRO.typeId());
    }

    @Test
    public void shouldNotTryWrapWhenLengthInsufficientForMinimumRequiredLength()
    {
        int offsetString1 = Long.BYTES + Short.BYTES;
        buffer.putByte(10 + offsetString1, (byte) 0);
        int offsetString2 = offsetString1 + Byte.BYTES + Integer.BYTES;
        buffer.putByte(10 + offsetString2, (byte) 1);
        for (int maxLimit=10; maxLimit < 10 + offsetString2 + Byte.BYTES; maxLimit++)
        {
            assertNull(flatRO.tryWrap(buffer,  10, maxLimit));
        }
    }

    @Test
    public void shouldNotWrapWhenLengthInsufficientForMinimumRequiredLength()
    {
        int offsetString1 = Long.BYTES + Short.BYTES;
        buffer.putByte(10 + offsetString1, (byte) 0);
        int offsetString2 = offsetString1 + Byte.BYTES + Integer.BYTES;
        buffer.putByte(10 + offsetString2, (byte) 1);
        for (int maxLimit=10; maxLimit < 10 + offsetString2 + Byte.BYTES; maxLimit++)
        {
            try
            {
                flatRO.wrap(buffer,  10, maxLimit);
                fail("Exception not thrown");
            }
            catch(Exception e)
            {
                if (!(e instanceof IndexOutOfBoundsException))
                {
                    fail("Unexpected exception " + e);
                }
            }
        }
    }

    @Test
    public void shouldTryWrapWhenLengthSufficientForMinimumRequiredLength()
    {
        int offsetString1 = Long.BYTES + Short.BYTES;
        buffer.putByte(10 + offsetString1, (byte) 0);
        int offsetString2 = offsetString1 + Byte.BYTES + Integer.BYTES;
        buffer.putByte(10 + offsetString2, (byte) 0);
        assertSame(flatRO, flatRO.tryWrap(buffer, 10, 10 + offsetString2 + Byte.BYTES));
    }

    @Test
    public void shouldWrapWhenLengthSufficientForMinimumRequiredLength()
    {
        int offsetString1 = Long.BYTES + Short.BYTES;
        buffer.putByte(10 + offsetString1, (byte) 0);
        int offsetString2 = offsetString1 + Byte.BYTES + Integer.BYTES;
        buffer.putByte(10 + offsetString2, (byte) 0);
        assertSame(flatRO, flatRO.wrap(buffer, 10, 10 + offsetString2 + Byte.BYTES));
    }

    @Test
    public void shouldRewrapAfterBuild()
    {
        flatRW.wrap(buffer, 0, 100)
                .fixed1(10)
                .string1("value1")
                .string2("value2")
                .build();

        final FlatFW flat = flatRW.rewrap()
                .fixed1(20)
                .string1("value3")
                .string2("value4")
                .build();

        assertSame(20L, flat.fixed1());
        assertEquals("value3", flat.string1().asString());
        assertEquals("value4", flat.string2().asString());
    }

    @Test
    public void shouldDefaultValues() throws Exception
    {
        int limit = flatRW.wrap(buffer, 0, 100)
                .fixed1(10)
                .string1("value1")
                .string2("value2")
                .build()
                .limit();
        flatRO.wrap(buffer,  0,  limit);
        assertEquals(222, flatRO.fixed2());
        assertEquals(333, flatRO.fixed3());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetFixed1WithInsufficientSpace()
    {
        flatRW.wrap(buffer, 10, 10)
               .fixed1(10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetFixed2WithInsufficientSpace()
    {
        flatRW.wrap(buffer, 10, 12)
                .fixed1(10)
                .fixed2(20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetString1WhenDefaultingFixed2ExceedsMaxLimit()
    {
        flatRW.wrap(buffer, 10, 12)
                .fixed1(10)
                .string1("");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetString1WhenExceedsMaxLimit()
    {
        flatRW.wrap(buffer, 10, 14)
                .fixed1(0x01)
                .fixed2(0x0101)
                .string1("1234");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetFixed3WithInsufficientSpace()
    {
        flatRW.wrap(buffer, 10, 15)
                .fixed1(10)
                .fixed2(20)
                .string1("")
                .fixed3(30);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToSetFixed2BeforeFixed1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
                .fixed2(10);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToSetString1BeforeFixed1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
                .string1("value1");
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToSetString2BeforeFixed1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
                .string2("value1");
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToSetString2BeforeString1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
                .fixed1(10)
                .string2("value1");
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToResetFixed1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
            .fixed1(10)
            .fixed1(101)
            .build();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToResetString1() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
            .fixed1(10)
            .fixed2(111)
            .string1("value1")
            .string1("another value")
            .build();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToBuildWhenFixed1NotSet() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
            .build();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToBuildWhenString1NotSet() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
            .fixed1(10)
            .build();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailToBuildWhenString2NotSet() throws Exception
    {
        flatRW.wrap(buffer, 0, 100)
            .fixed1(10)
            .fixed2(111)
            .string1("value1")
            .fixed3(33)
            .build();
    }

    @Test
    public void shouldSetAllValues() throws Exception
    {
        flatRW.wrap(buffer, 0, buffer.capacity())
                .fixed1(10)
                .fixed2(20)
                .string1("value1")
                .fixed3(30)
                .string2("value2")
                .build();
        flatRO.wrap(buffer,  0,  100);
        assertEquals(10, flatRO.fixed1());
        assertEquals(20, flatRO.fixed2());
        assertEquals("value1", flatRO.string1().asString());
        assertEquals(30, flatRO.fixed3());
        assertEquals("value2", flatRO.string2().asString());
    }

    @Test
    public void shouldSetStringValuesUsingStringFW() throws Exception
    {
        FlatFW.Builder builder = flatRW.wrap(buffer, 0, buffer.capacity());
        builder.fixed1(10)
               .fixed2(20);
        StringFW value = stringRW.wrap(valueBuffer,  0, valueBuffer.capacity())
               .set("value1", UTF_8)
               .build();
        builder.string1(value)
               .fixed3(30);
        value = stringRW.wrap(valueBuffer,  0, valueBuffer.capacity())
               .set("value2", UTF_8)
               .build();
        builder.string2(value)
               .build();
        flatRO.wrap(buffer,  0,  100);
        assertEquals(10, flatRO.fixed1());
        assertEquals(20, flatRO.fixed2());
        assertEquals("value1", flatRO.string1().asString());
        assertEquals(30, flatRO.fixed3());
        assertEquals("value2", flatRO.string2().asString());
    }

    @Test
    public void shouldSetStringValuesUsingBuffer() throws Exception
    {
        valueBuffer.putStringWithoutLengthUtf8(0, "value1");
        valueBuffer.putStringWithoutLengthUtf8(10, "value2");
        flatRW.wrap(buffer, 0, buffer.capacity())
            .fixed1(10)
            .fixed2(20)
            .string1(valueBuffer, 0, 6)
            .fixed3(30)
            .string2(valueBuffer, 10, 6)
            .build();
        flatRO.wrap(buffer,  0,  100);
        assertEquals(10, flatRO.fixed1());
        assertEquals(20, flatRO.fixed2());
        assertEquals("value1", flatRO.string1().asString());
        assertEquals(30, flatRO.fixed3());
        assertEquals("value2", flatRO.string2().asString());
    }

    @Test
    public void shouldSetStringValuesToNull() throws Exception
    {
        final int offset = 0;
        int limit = flatRW.wrap(buffer, offset, buffer.capacity())
            .fixed1(10)
            .string1((String) null)
            .string2((StringFW) null)
            .build()
            .limit();
        expected.putLong(offset, 10);
        expected.putShort(offset + 8, (short) 222);
        expected.putByte(offset + 10, (byte) -1);
        expected.putInt(offset + 11, 333);
        expected.putByte(offset + 15, (byte) -1);

        assertEquals(expected.byteBuffer(), buffer.byteBuffer());

        flatRO.wrap(buffer, offset, limit);
        assertNull(flatRO.string1().asString());
        assertNull(flatRO.string2().asString());
    }

    @Test
    public void shouldSetStringValuesToEmptyString() throws Exception
    {
        int limit = flatRW.wrap(buffer, 0, buffer.capacity())
            .fixed1(10)
            .fixed2(20)
            .string1("")
            .fixed3(30)
            .string2("")
            .build()
            .limit();
        flatRO.wrap(buffer,  0,  limit);
        assertEquals("", flatRO.string1().asString());
        assertEquals("", flatRO.string2().asString());
    }

}
