import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SHA {

    private static final int h[] =
            {0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
                    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19};

    private static final int k[] = {0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
            0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
            0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
            0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
            0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
            0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
            0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
            0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

    private String message;
    private byte[] messageInBytes;

    public static void main(String[] args) {
        new SHA();
    }

    public SHA() {
        this.message = loadMessage();
        this.messageInBytes = basicChanges();
        make4BytesParts();
        System.out.println("Final: " +  finalHash());
    }

    private String loadMessage() {
        return "Test message bsadsadsasadsadsadssasasadsadsadsadsad lorem ipsum dolor sit amet";
    }

    //Obróbka wstępna:
    private byte[] basicChanges() {

        boolean addedOne = false;
        byte[] bytes = message.getBytes();

        while (bytes.length % 64 != 56) {
            byte[] newBytes = new byte[bytes.length + 1];
            System.arraycopy(bytes,0,newBytes,0, bytes.length);

            if (!addedOne) {
                newBytes[newBytes.length - 1] = (byte) 0x80;
                addedOne = true;
            } else {
                newBytes[newBytes.length - 1] = (byte) 0x00;
            }

            bytes = newBytes;
        }

        long a = message.length() * 8;

        ByteBuffer wrapper = ByteBuffer.allocate(Long.BYTES);
        wrapper.putLong(a);

        byte[] newBytes = new byte[bytes.length + wrapper.array().length];
        System.arraycopy(bytes,0,newBytes,0, bytes.length);
        System.arraycopy(wrapper.array(),0, newBytes,bytes.length, wrapper.array().length);

        bytes = newBytes;

        return bytes;
    }

    //podziel wiadomość na 512-bitowe kawałki
    private List<byte[]> make64BytesParts() {
        List<byte[]> list = new ArrayList<>();
        int length;
        byte[] bytesArray;

        for (int i = 0; i < messageInBytes.length; i+=64) {
            length = Math.min(64, messageInBytes.length - i);
            bytesArray = new byte[length];
            System.arraycopy(messageInBytes, i, bytesArray, 0, length);
            list.add(bytesArray);
        }

        return list;
    }

    //dla  każdego kawałka
    //podziel kawałek na szesnaście 32-bitowych słów big-endian w [0..15]
    private void make4BytesParts() {
        List<byte[]> list64Bytes = make64BytesParts();
        List<Integer> list4Bytes;
        byte[] bytesArray;
        ByteBuffer wrapper;

        for (byte[] bytes : list64Bytes) {
            list4Bytes = new ArrayList<>();
            for (int i = 0; i < bytes.length; i+=4) {
                bytesArray = new byte[4];
                System.arraycopy(bytes, i, bytesArray, 0, 4);
                wrapper = ByteBuffer.wrap(bytesArray);
                list4Bytes.add(wrapper.getInt());
            }

            extend32To64(list4Bytes);
        }
    }

    //Rozszerz szesnaście 32-bitowych słów na sześćdziesiąt cztery 32-bitowe słowa:
    private void extend32To64(List<Integer> list) {

        int s0, s1;

        for (int i = 16; i < 64; i++) {
            Integer w15 = list.get(i - 15);
            Integer w2 = list.get(i - 2);

            s0 = Integer.rotateRight(w15, 7) ^
                    Integer.rotateRight(w15, 18) ^
                    w15 >>> 3;

            s1 = Integer.rotateRight(w2, 17) ^
                    Integer.rotateRight(w2, 19) ^
                    w2 >>> 10;

            list.add(i, list.get(i - 16) + s0 + list.get(i -7) + s1);
        }

        makeHash(list);
    }

    private void makeHash(List<Integer> list) {
        int a = h[0], b = h[1], c = h[2], d = h[3], e = h[4], f = h[5], g = h[6], hh = h[7];
        int s0, s1, ch, t1, maj, t2;

        for (int i = 0; i < list.size(); i ++) {
            s1 = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25);
            ch = (e & f) ^ (~e & g);
            t1 = hh + s1 + ch + k[i] + list.get(i);
            s0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
            maj = (a & b) ^ (a & c) ^ (b & c);
            t2 = s0 + maj;

            hh = g;
            g = f;
            e = d + t1;
            d = c;
            c = b;
            b = a;
            a = t1 + t2;
        }

        h[0] = h[0] + a;
        h[1] = h[1] + b;
        h[2] = h[2] + c;
        h[3] = h[3] + d;
        h[4] = h[4] + e;
        h[5] = h[5] + f;
        h[6] = h[6] + g;
        h[7] = h[7] + hh;
    }

    private String finalHash() {
        String digest = Integer.toHexString(h[0]) +
                Integer.toHexString(h[1]) +
                Integer.toHexString(h[3]) +
                Integer.toHexString(h[3]) +
                Integer.toHexString(h[4]) +
                Integer.toHexString(h[5]) +
                Integer.toHexString(h[6]) +
                Integer.toHexString(h[7]);

        return digest;
    }
}