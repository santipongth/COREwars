import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class badAgent {
    private int step;
    private String name;
    private List<Integer> counts;
    private List<String> owners;
    private int newFernies;
    public badAgent(int step, String name, int newFernies) {
        this.step = step;
        this.name = name;
        this.newFernies = newFernies;
        this.counts = new ArrayList<>();
        this.owners = new ArrayList<>();
    }

    public void read_state() {

        try (BufferedReader reader = new BufferedReader(new FileReader(this.name+"/"+this.step+".txt"))) {
            String line;
            line = reader.readLine();
            String[] parts = line.split(","); // Split line into parts
                // Process the parts array as needed
            for (String part : parts) {
                int number = Integer.parseInt(part);
                counts.add(number);
            }
            line = reader.readLine();
            parts = line.split(","); // Split line into parts
                // Process the parts array as needed
            for (String part : parts) {
                owners.add(part);
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    public void update_state() {
        //make moves and save them to a/move.txt
        //move.txt should be a simple list of where new units are being placed and removed from
        //i.e. : 0,50,-10,-10,-10,0,-10,0,0,-10
        int[] nonNegativeIndices = findNonNegativeIndices(this.counts);
        Random random = new Random();
        int placement = nonNegativeIndices[random.nextInt(nonNegativeIndices.length)];
        int most = newFernies;
        String move = placement+","+most;
        String move2 = random.nextInt(owners.size())+",25";
        int[] ownedIndices = findOwnedIndices(this.owners);
        int removal = 8;//ownedIndices[random.nextInt(ownedIndices.length)];
        String move3 = removal+",-25";
        //System.out.println("Agent b's movement: " + move);
        try {
            FileWriter writer = new FileWriter(this.name+"/move.txt");
            writer.write(move);
            writer.append("\n" + move2);
            writer.append("\n" + move3);
            writer.close(); // Always close the writer to finalize the output and free resources
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }

    public static int[] findNonNegativeIndices(List<Integer> list) {
        return IntStream.range(0, list.size())  // Generates a stream of indices
                .filter(i -> list.get(i) >= 0)  // Filters indices where the corresponding list value is non-negative
                .toArray();                    // Converts the stream to an array
    }

    public static int[] findOwnedIndices(List<String> list) {
        return IntStream.range(0, list.size())  // Generates a stream of indices
                .filter(i -> list.get(i).equals("Y"))  // Filters indices where the corresponding list value is non-negative
                .toArray();                    // Converts the stream to an array
    }
  
    public static void main(String[] args) {
        //System.out.println("This is a test of agent a: " + args);
        badAgent me = new badAgent(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
        me.read_state();
        me.update_state();
    }
}

