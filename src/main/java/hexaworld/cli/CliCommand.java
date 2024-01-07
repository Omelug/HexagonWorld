package hexaworld.cli;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CliCommand {
  private final String cmd, description;
  @Override
  public String toString() {
    return cmd;
  }
}
