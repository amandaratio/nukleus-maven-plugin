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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.reaktivity.reaktor.internal.test.types.inner.UnionOctetsFW;

public class UnionOctetsFWTest
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
    private final UnionOctetsFW.Builder flyweightRW = new UnionOctetsFW.Builder();
    private final UnionOctetsFW flyweigthRO = new UnionOctetsFW();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    static int setAllTestValuesCase1(MutableDirectBuffer buffer, final int offset)
    {
        int pos = offset;
        buffer.putByte(pos, (byte) 1);
        buffer.putStringWithoutLengthUtf8(pos += 1, "1234");
        return pos - offset + "1234".length();
    }

    static int setAllTestValuesCase2(MutableDirectBuffer buffer, final int offset)
    {
        int pos = offset;
        buffer.putByte(pos, (byte) 2);
        buffer.putStringWithoutLengthUtf8(pos += 1, "1234567890123456");
        return pos - offset + "1234567890123456".length();
    }

    static int setAllTestValuesCase3(MutableDirectBuffer buffer, final int offset)
    {
        int pos = offset;
        buffer.putByte(pos, (byte) 3);
        buffer.putByte(pos += 1, (byte) "valueOfString1".length());
        buffer.putStringWithoutLengthUtf8(pos += 1, "valueOfString1");
        return pos - offset + "valueOfString1".length();
    }

    static void assertAllTestValuesReadCase1(UnionOctetsFW flyweight)
    {
        assertEquals("1234", flyweight.octets4().get((b, o, m) -> b.getStringWithoutLengthUtf8(o, m - o)));
        assertEquals(0, flyweight.octets16().sizeof());
        assertEquals(null, flyweight.string1().asString());
    }

    static void assertAllTestValuesReadCase2(UnionOctetsFW flyweight)
    {
        assertEquals("1234567890123456", flyweight.octets16().get((b, o, m) -> b.getStringWithoutLengthUtf8(o, m - o)));
        assertEquals(0, flyweight.octets4().sizeof());
        assertEquals(null, flyweight.string1().asString());
    }

    static void assertAllTestValuesReadCase3(UnionOctetsFW flyweight)
    {
        assertEquals("valueOfString1", flyweight.string1().asString());
        assertEquals(0, flyweight.octets4().sizeof());
        assertEquals(0, flyweight.octets16().sizeof());
    }

    @Test
    public void shouldNotTryWrapWhenIncompleteCase1()
    {
        int size = setAllTestValuesCase1(buffer, 10);
        for (int maxLimit=10; maxLimit < 10 + size; maxLimit++)
        {
            assertNull(flyweigthRO.tryWrap(buffer,  10, maxLimit));
        }
    }

    @Test
    public void shouldNotWrapWhenIncompleteCase1()
    {
        int size = setAllTestValuesCase1(buffer, 10);
        for (int maxLimit=10; maxLimit < 10 + size; maxLimit++)
        {
            try
            {
                flyweigthRO.wrap(buffer,  10, maxLimit);
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
    public void shouldTryWrapWhenLengthSufficientCase1()
    {
        int size = setAllTestValuesCase1(buffer, 10);
        assertSame(flyweigthRO, flyweigthRO.tryWrap(buffer, 10, 10 + size));
    }

    @Test
    public void shouldWrapWhenLengthSufficientCase1()
    {
        int size = setAllTestValuesCase1(buffer, 10);
        assertSame(flyweigthRO, flyweigthRO.wrap(buffer, 10, 10 + size));
    }

    @Test
    public void shouldTryWrapAndReadAllValuesCase1() throws Exception
    {
        final int offset = 1;
        setAllTestValuesCase1(buffer, offset);
        assertNotNull(flyweigthRO.tryWrap(buffer, offset, buffer.capacity()));
        assertAllTestValuesReadCase1(flyweigthRO);
    }

    @Test
    public void shouldWrapAndReadAllValuesCase1() throws Exception
    {
        final int offset = 1;
        setAllTestValuesCase1(buffer, offset);
        flyweigthRO.wrap(buffer, offset, buffer.capacity());
        assertAllTestValuesReadCase1(flyweigthRO);
    }

    @Test
    public void shouldSetOctets4()
    {
        int limit = flyweightRW.wrap(buffer, 10, buffer.capacity())
               .octets4(b -> b.put("1234".getBytes(UTF_8)))
               .build()
               .limit();
        int size = setAllTestValuesCase1(expected, 10);
        assertEquals(10 + size, limit);
        assertEquals(expected.byteBuffer(), buffer.byteBuffer());
    }

    @Test
    public void shouldSetOctets16()
    {
        int limit = flyweightRW.wrap(buffer, 0, buffer.capacity())
               .octets16(b -> b.put("1234567890123456".getBytes(UTF_8)))
               .build()
               .limit();
        flyweigthRO.wrap(buffer,  0,  limit);
        assertAllTestValuesReadCase2(flyweigthRO);
    }

    @Test
    public void shouldSetString1()
    {
        int limit = flyweightRW.wrap(buffer, 0, buffer.capacity())
            .string1("valueOfString1")
            .build()
            .limit();
        flyweigthRO.wrap(buffer,  0,  limit);
        assertAllTestValuesReadCase3(flyweigthRO);
    }

    @Test
    public void shouldSetStringWithValueNull()
    {
        int limit = flyweightRW.wrap(buffer, 10, buffer.capacity())
            .string1(null).build().limit();
        flyweigthRO.wrap(buffer,  0,  limit);
        assertEquals(null, flyweigthRO.string1().asString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetOctets4WithInsufficientSpace()
    {
        flyweightRW.wrap(buffer, 10, 14)
               .octets4(b -> b.put("1234".getBytes(UTF_8)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetOctets4WithValueTooLong()
    {
        flyweightRW.wrap(buffer, 10, buffer.capacity())
               .octets4(b -> b.put("12345".getBytes(UTF_8)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetOctets16WithInsufficientSpace()
    {
        flyweightRW.wrap(buffer, 10, 26)
               .octets16(b -> b.put("1234567890123456".getBytes(UTF_8)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToSetOctets16WithValueToLong()
    {
        flyweightRW.wrap(buffer, 10, buffer.capacity())
               .octets16(b -> b.put("12345678901234567".getBytes(UTF_8)));
    }

    @Test
    public void shouldBuildWithNothingSet()
    {
        int limit = flyweightRW.wrap(buffer, 10, buffer.capacity())
            .build()
            .limit();
        flyweigthRO.wrap(buffer,  0,  limit);
        assertEquals(0, flyweigthRO.octets16().sizeof());
        assertEquals(0, flyweigthRO.octets4().sizeof());
    }

}
