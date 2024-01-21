package tests.example.hexaworld;

import java.util.ArrayList;
import java.util.List;

public class ListTest {
  public static void main(String[] args) {
    List<Integer> intList = new ArrayList<>();
    intList.add(1);
    intList.add(2);
    intList.add(3);

    System.out.println("" + intList.get(1));
  }
}
