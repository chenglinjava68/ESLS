package com.datagroup.ESLS.common.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
@Data
@NoArgsConstructor
@ToString
public class ByteResponse {

    private byte[] firstByte;
    private List<byte[]> byteList;

    public ByteResponse(byte[] firstByte, List<byte[]> byteList) {
        this.firstByte = firstByte;
        this.byteList = byteList;
    }
}
