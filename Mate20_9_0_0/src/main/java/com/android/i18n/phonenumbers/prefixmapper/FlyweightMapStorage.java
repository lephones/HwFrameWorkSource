package com.android.i18n.phonenumbers.prefixmapper;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

final class FlyweightMapStorage extends PhonePrefixMapStorageStrategy {
    private static final int INT_NUM_BYTES = 4;
    private static final int SHORT_NUM_BYTES = 2;
    private int descIndexSizeInBytes;
    private ByteBuffer descriptionIndexes;
    private String[] descriptionPool;
    private ByteBuffer phoneNumberPrefixes;
    private int prefixSizeInBytes;

    FlyweightMapStorage() {
    }

    public int getPrefix(int index) {
        return readWordFromBuffer(this.phoneNumberPrefixes, this.prefixSizeInBytes, index);
    }

    public String getDescription(int index) {
        return this.descriptionPool[readWordFromBuffer(this.descriptionIndexes, this.descIndexSizeInBytes, index)];
    }

    public void readFromSortedMap(SortedMap<Integer, String> phonePrefixMap) {
        SortedSet<String> descriptionsSet = new TreeSet();
        this.numOfEntries = phonePrefixMap.size();
        this.prefixSizeInBytes = getOptimalNumberOfBytesForValue(((Integer) phonePrefixMap.lastKey()).intValue());
        this.phoneNumberPrefixes = ByteBuffer.allocate(this.numOfEntries * this.prefixSizeInBytes);
        int index = 0;
        for (Entry<Integer, String> entry : phonePrefixMap.entrySet()) {
            int prefix = ((Integer) entry.getKey()).intValue();
            storeWordInBuffer(this.phoneNumberPrefixes, this.prefixSizeInBytes, index, prefix);
            this.possibleLengths.add(Integer.valueOf(((int) Math.log10((double) prefix)) + 1));
            descriptionsSet.add(entry.getValue());
            index++;
        }
        createDescriptionPool(descriptionsSet, phonePrefixMap);
    }

    private void createDescriptionPool(SortedSet<String> descriptionsSet, SortedMap<Integer, String> phonePrefixMap) {
        this.descIndexSizeInBytes = getOptimalNumberOfBytesForValue(descriptionsSet.size() - 1);
        this.descriptionIndexes = ByteBuffer.allocate(this.numOfEntries * this.descIndexSizeInBytes);
        this.descriptionPool = new String[descriptionsSet.size()];
        descriptionsSet.toArray(this.descriptionPool);
        int index = 0;
        for (int i = 0; i < this.numOfEntries; i++) {
            storeWordInBuffer(this.descriptionIndexes, this.descIndexSizeInBytes, index, Arrays.binarySearch(this.descriptionPool, (String) phonePrefixMap.get(Integer.valueOf(readWordFromBuffer(this.phoneNumberPrefixes, this.prefixSizeInBytes, i)))));
            index++;
        }
    }

    public void readExternal(ObjectInput objectInput) throws IOException {
        int i;
        this.prefixSizeInBytes = objectInput.readInt();
        this.descIndexSizeInBytes = objectInput.readInt();
        int sizeOfLengths = objectInput.readInt();
        this.possibleLengths.clear();
        int i2 = 0;
        for (i = 0; i < sizeOfLengths; i++) {
            this.possibleLengths.add(Integer.valueOf(objectInput.readInt()));
        }
        i = objectInput.readInt();
        if (this.descriptionPool == null || this.descriptionPool.length < i) {
            this.descriptionPool = new String[i];
        }
        while (i2 < i) {
            this.descriptionPool[i2] = objectInput.readUTF();
            i2++;
        }
        readEntries(objectInput);
    }

    private void readEntries(ObjectInput objectInput) throws IOException {
        this.numOfEntries = objectInput.readInt();
        if (this.phoneNumberPrefixes == null || this.phoneNumberPrefixes.capacity() < this.numOfEntries) {
            this.phoneNumberPrefixes = ByteBuffer.allocate(this.numOfEntries * this.prefixSizeInBytes);
        }
        if (this.descriptionIndexes == null || this.descriptionIndexes.capacity() < this.numOfEntries) {
            this.descriptionIndexes = ByteBuffer.allocate(this.numOfEntries * this.descIndexSizeInBytes);
        }
        for (int i = 0; i < this.numOfEntries; i++) {
            readExternalWord(objectInput, this.prefixSizeInBytes, this.phoneNumberPrefixes, i);
            readExternalWord(objectInput, this.descIndexSizeInBytes, this.descriptionIndexes, i);
        }
    }

    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.prefixSizeInBytes);
        objectOutput.writeInt(this.descIndexSizeInBytes);
        objectOutput.writeInt(this.possibleLengths.size());
        Iterator it = this.possibleLengths.iterator();
        while (it.hasNext()) {
            objectOutput.writeInt(((Integer) it.next()).intValue());
        }
        objectOutput.writeInt(this.descriptionPool.length);
        int i = 0;
        for (String description : this.descriptionPool) {
            objectOutput.writeUTF(description);
        }
        objectOutput.writeInt(this.numOfEntries);
        while (true) {
            int i2 = i;
            if (i2 < this.numOfEntries) {
                writeExternalWord(objectOutput, this.prefixSizeInBytes, this.phoneNumberPrefixes, i2);
                writeExternalWord(objectOutput, this.descIndexSizeInBytes, this.descriptionIndexes, i2);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    private static int getOptimalNumberOfBytesForValue(int value) {
        return value <= 32767 ? 2 : 4;
    }

    private static void readExternalWord(ObjectInput objectInput, int wordSize, ByteBuffer outputBuffer, int index) throws IOException {
        int wordIndex = index * wordSize;
        if (wordSize == 2) {
            outputBuffer.putShort(wordIndex, objectInput.readShort());
        } else {
            outputBuffer.putInt(wordIndex, objectInput.readInt());
        }
    }

    private static void writeExternalWord(ObjectOutput objectOutput, int wordSize, ByteBuffer inputBuffer, int index) throws IOException {
        int wordIndex = index * wordSize;
        if (wordSize == 2) {
            objectOutput.writeShort(inputBuffer.getShort(wordIndex));
        } else {
            objectOutput.writeInt(inputBuffer.getInt(wordIndex));
        }
    }

    private static int readWordFromBuffer(ByteBuffer buffer, int wordSize, int index) {
        int wordIndex = index * wordSize;
        return wordSize == 2 ? buffer.getShort(wordIndex) : buffer.getInt(wordIndex);
    }

    private static void storeWordInBuffer(ByteBuffer buffer, int wordSize, int index, int value) {
        int wordIndex = index * wordSize;
        if (wordSize == 2) {
            buffer.putShort(wordIndex, (short) value);
        } else {
            buffer.putInt(wordIndex, value);
        }
    }
}
