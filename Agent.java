import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Agent {
    private int step;
    private String name;
    private List<Integer> counts;
    private List<String> owners;
    private int newFernies;
    public Agent(int step, String name, int newFernies) {
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
        Random random = new Random();
        int placement = random.nextInt(owners.size());
        String move = placement+","+50;
        //System.out.println("Agent b's movement: " + move);
        try {
            FileWriter writer = new FileWriter(this.name+"/move.txt");
            writer.write(move);
            //writer.append("\nThis is another line of text.");
            writer.close(); // Always close the writer to finalize the output and free resources
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
  
    public static void main(String[] args) {
        //System.out.println("This is a test of agent a: " + args);
        Agent me = new Agent(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
        me.read_state();
        me.update_state();
    }
}
