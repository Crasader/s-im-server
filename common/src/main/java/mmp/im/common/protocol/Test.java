package mmp.im.common.protocol;


import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;

public class Test {
    public static void main(String[] args) {
        // 序列化
        AddressBookProtos.Person.Builder person = AddressBookProtos.Person.newBuilder();
        person.setEmail("xxxxxxxx@qq.com").setId(1).setName("张三");

        AddressBookProtos.Person.PhoneNumber.Builder number = AddressBookProtos.Person.PhoneNumber.newBuilder();
        AddressBookProtos.Person.PhoneType type = AddressBookProtos.Person.PhoneType.HOME;
        person.setType(type);
        person.getType().getNumber();
        number.setNumber("XXXXXXXX");
        person.addPhones(number);

        AddressBookProtos.Person.PhoneNumber phoneNumber = AddressBookProtos.Person.PhoneNumber.newBuilder().build();
        // 包装
        person.setData(Any.pack(phoneNumber));

        System.out.println(person.getData().getTypeUrl());

        try {
            AddressBookProtos.Person.PhoneNumber p = person.getData().unpack(AddressBookProtos.Person.PhoneNumber.class);
            System.out.println(p.getNumber());

        } catch (Exception e) {
            e.printStackTrace();
        }

        person.putProtoMap("k", "v");

        AddressBookProtos.AddressBook.Builder address = AddressBookProtos.AddressBook.newBuilder();
        address.addPeople(person);
        address.addPeople(person);

        AddressBookProtos.AddressBook book = address.build();
        byte[] result = book.toByteArray(); // 序列化

        // 反序列化
        AddressBookProtos.AddressBook read;
        try {
            read = AddressBookProtos.AddressBook.parseFrom(result);
            // 转成中文
            System.out.println(read.getPeopleList().get(0).getNameBytes().toStringUtf8());
            System.out.println(read);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

}

 /*
        people{
            name:"\345\274\240\344\270\211"
            id:1
            email:"xxxxxxxx@qq.com"
            phones{
                number:"XXXXXXXX"
            }
        }
        people{
            name:"\345\274\240\344\270\211"
            id:1
            email:"xxxxxxxx@qq.com"
            phones{
                number:"XXXXXXXX"
            }
        }
  */