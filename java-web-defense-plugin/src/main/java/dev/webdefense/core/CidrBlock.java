package dev.webdefense.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public final class CidrBlock {
    private final byte[] network;
    private final int prefixBits;
    private CidrBlock(byte[] network, int prefixBits) { this.network=network; this.prefixBits=prefixBits; }
    public static CidrBlock parse(String text) {
        String[] parts=text.trim().split("/",2);
        try {
            byte[] address=InetAddress.getByName(parts[0]).getAddress();
            int maxBits=address.length*8;
            int prefix=parts.length==2?Integer.parseInt(parts[1]):maxBits;
            if(prefix<0||prefix>maxBits) throw new IllegalArgumentException("Invalid prefix");
            byte[] masked=Arrays.copyOf(address,address.length); applyMask(masked,prefix);
            return new CidrBlock(masked,prefix);
        } catch(UnknownHostException|NumberFormatException e) { throw new IllegalArgumentException("Invalid IP/CIDR: "+text,e); }
    }
    public boolean contains(String ip) {
        try {
            byte[] candidate=InetAddress.getByName(ip).getAddress();
            if(candidate.length!=network.length) return false;
            applyMask(candidate,prefixBits); return Arrays.equals(candidate,network);
        } catch(UnknownHostException e) { return false; }
    }
    private static void applyMask(byte[] bytes,int prefixBits) {
        int full=prefixBits/8, partial=prefixBits%8;
        if(partial!=0&&full<bytes.length){ int mask=0xFF<<(8-partial); bytes[full]=(byte)(bytes[full]&mask); full++; }
        for(int i=full;i<bytes.length;i++) bytes[i]=0;
    }
}
