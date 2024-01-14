package hexaworld.net;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.ByteArrayOutputStream;

public class Change{
  @Getter
  @AllArgsConstructor
  public enum CHANGE{
    POSITION(false), ENERGY(false), STOP(false);//Stop is only change for stop accepting ticks
    final boolean hasData;
  }
  final CHANGE change;
  public Change(CHANGE change){
    this.change = change;
  }
}