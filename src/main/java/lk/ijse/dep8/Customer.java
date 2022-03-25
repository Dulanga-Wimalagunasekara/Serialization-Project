package lk.ijse.dep8;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class Customer implements Serializable {
    private String id;
    private String name;
    private String address;
    private byte[] bytes;

    public Customer() {
    }

    public Customer(String id, String name, String address, byte[] bytes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.bytes = bytes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }

    public void printDetails(){
        System.out.printf("id=%s, name=%s, address=%s\n", id, name, address);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }



}
