import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Simulation {
    private int scale;
    public World world; //maybe should be public
    public String[] agents;
    public List<String> agent_names;
    public HashSet<String> active_agents;
    public ArrayList<World_State> state_history;
    public int superStep; //the total number of steps counting each players steps separately.
    public int active_step; //the agent currently being moved
    public HashMap<String, Agent_Details> agentLookup;
    public int soldiersPerTurn;
    public int visibility_range;
    public Simulation(int scale, String[] agents, HashMap<String, Agent_Details> agentLookup, int perTurn, int maxx, int startCount, int visibility_range){
        this.scale = scale;
        this.superStep = 0;
        //this.agents = new String[] {"a","b"}; //Will be original names. Anon names will be handled on the world side..
        this.agents = agents;
        this.agentLookup = agentLookup;
        this.agent_names = Arrays.asList(this.agents);
        this.active_agents = new LinkedHashSet<>(this.agent_names);
        this.soldiersPerTurn = perTurn;
        this.visibility_range = visibility_range;
        this.world = new World(this.scale, agents, maxx, startCount, visibility_range);
        this.active_step = 0;
        this.state_history = new ArrayList<>();
    }

    public void update_state(String agent_name, int step) {
        //creates the file of the most recent state
        ArrayList<Movement> moves  = readMove(agent_name);
        List<World.Node_State> myView = this.world.get_perspective(agent_name);
        String counts = "";
        String owners = "";
        for (World.Node_State s: myView) {
            counts = counts + s.count() + ",";
            owners = owners + s.owner() + ",";
        }
        String c_string = counts.substring(0, counts.length() - 1);
        String o_string = owners.substring(0, owners.length() - 1);
        try {
            FileWriter writer = new FileWriter(agent_name+"/"+step+".txt");
            writer.write(c_string);
            writer.append("\n" + o_string);
            //append new soldier count here..Followed by max soldiers
            for (Movement m : moves) {
                Integer key = m.loc;
                Integer value = m.change;
                writer.append("\n" + key + "," + value);
            }
            writer.close(); // Always close the writer to finalize the output and free resources
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
        //some function appendMove() which adds the move.txt file to the end of this file.
    }

    public ArrayList<World_State> get_state_history() {
        return this.state_history;
    }

    public void move(String agent_name) {
    //Updates world with a given agents moves.
        ArrayList<Movement> movements = readMove(agent_name);
        if (check_legal(movements)) {
            //movements are legal
            for (Movement m : movements) {
                int gloc = world.get_global_perspective(m.loc, agent_name);
                int scount = m.change;
                world.nodes.get(gloc).addSoldiers(agent_name, scount); //local battles happen at nodes
            }      
        } else {
            System.out.println("ILLEGAL MOVE: " + agent_name);
        }
    }

    public void battle(String agent_name, int current_step) {
        //calls world.resolve() or world.get_losers() if no edge battles happen..
        //Updates which players are still in the game..
        int isRight = current_step%2;
        Set<String> losers = world.resolve(agent_name,isRight);
        //System.out.println("LOSERS: ");
        //System.out.println(Arrays.toString(losers.toArray()));
        //System.out.println("CURRENT ACTIVE:");
        //System.out.println(Arrays.toString(active_agents.toArray()));
        active_agents.removeAll(losers);
        //System.out.println("NEW ACTIVE:");
        //System.out.println(Arrays.toString(active_agents.toArray()));
        //System.out.println("okkkkkkkk");
    }

    public ArrayList<Movement> readMove(String agent_name) {
    //reads a given agents move
    //agent name is their actually name / folder loc
    ArrayList<Movement> movements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(agent_name+"/move.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // Split line into parts
                // Process the parts array as needed
                Movement m = new Movement(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),agent_name);
                movements.add(m); //location
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return movements;
    }

    public Boolean check_legal(ArrayList<Movement> movements) {
        //Checks whether a set of moves was legal
        int total_moved = 0;
        for (Movement m : movements) {
            total_moved = total_moved + m.change;
            if (m.change < 0) {
                int gloc = world.get_global_perspective(m.loc, m.agent_name);
                String anonOwner = world.nodes.get(gloc).getOwner();
                if (world.getAnonName(m.agent_name).equals(anonOwner)) {
                    if (world.nodes.get(gloc).getSoldiers() >= -1*m.change) {
                        //There are enough soldiers there to make the removal.
                        return true;
                    } else {return false;}
                } else {
                    //tried to remove from a different player.
                    return false;
                }
            }
        }
        if (total_moved <= soldiersPerTurn) {
            return true;
        } else {
            return false;
        }
    }

    public void commandAgent(String agent_name, int step) {
        //name should be the class file name of the agent
        //for testing
        Agent_Details agentD = this.agentLookup.get(agent_name);
        String agent_loc = agentD.getlocName();
        String agent_filename = agentD.getFileName();
        try {
            // Define the command and arguments in a list
            List<String> commands = new ArrayList<>();
            commands.add("java");
            //commands.add("-cp");
            //commands.add(".\\"+agent_loc+"\\");
            commands.add(agent_filename);
            commands.add(Integer.toString(step));
            commands.add(agent_loc);
            commands.add(Integer.toString(soldiersPerTurn)); //new soldiers count.
            //add on stuff about new number of agents etc

            //Create a ProcessBuilder
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true); // Redirect error stream to the output stream

            // Start the process
            long startTime = System.nanoTime();
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete and get the exit value
            int exitValue = process.waitFor();
            long endTime = System.nanoTime();
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            System.out.println("Process exited with code " + exitValue + " and took " + durationMillis + " milliseconds.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

public void make_turns(int current_step) {
    //take a turn for each player
    for (String agent_name : this.agent_names) { //this.active_agents gets modified in the for loop that may be an issue?
        if (this.active_agents.contains(agent_name)) {
            this.superStep++;
            update_state(agent_name,current_step);
            commandAgent(agent_name,current_step);
            move(agent_name);
            battle(agent_name,current_step);
        }
    }
}

public void make_turn(int current_step) {
    //take a turn for one player
    String agent_name = this.agents[this.active_step];
    System.out.println("Active Agents at Step "+ current_step+ " are " + Arrays.toString(active_agents.toArray()) + " and moving agent is: " + agent_name);
    if (this.active_agents.contains(agent_name)) {
        
        update_state(agent_name,current_step);
        commandAgent(agent_name,current_step);
        move(agent_name);
        battle(agent_name,current_step);
    }
    this.superStep++;
    this.active_step++;
    this.active_step = this.active_step%this.agents.length;
}

public void run(int steps) {
    //run for some number of steps
    for (String agent_name : this.agent_names) {update_state(agent_name,0);}
    world.printNodes();
    for (int i = 0; i < steps; i++) {
        make_turns(i);
        System.out.println(i);
        world.printNodes();
        if (active_agents.size() == 1) {
            System.out.println("Agent " + active_agents.toArray()[0] + " WINS!!!");
            break;
        }        
    }
}

public void setup() {
    //only run if using the run once or run one agent options
    //soldiersPerTurn = perTurn;
    for (String agent_name : this.agent_names) {
        try {
            FileWriter writer = new FileWriter(agent_name+"/move.txt");
            writer.close();
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
        update_state(agent_name,0);
    }
    World_State ws = new World_State(world, 0, superStep);
    state_history.add(ws);
}

public void run_once(int current_step) {
    //run all agents for one step
    world.printNodes();
    make_turns(current_step);
    System.out.println(current_step);
    world.printNodes();
    if (active_agents.size() == 1) {
        System.out.println("Agent " + active_agents.toArray()[0] + " WINS!!!");
    }        
    
}

public int run_one_agent(int current_step) {
    //run one agent once
    world.printNodes();
    make_turn(current_step);
    System.out.println(current_step);
    world.printNodes();
    if (active_agents.size() == 1) {
        System.out.println("Agent " + active_agents.toArray()[0] + " WINS!!!");
    }
    World_State ws = new World_State(world, current_step, superStep);
    state_history.add(ws);
    if (this.active_step == 0) {
        return 1;
    }
    return 0;
}

public static class Movement {
    public int loc;
    public int change;
    public String agent_name;
    public Movement(int loc, int change, String agent_name) {
        this.loc = loc;
        this.change = change;
        this.agent_name = agent_name;
    }
}

public static class Agent_Details {
    public String filename;
    public String locname;
    public Color color;
    public Agent_Details(String filename, String locname, Color color) {
        this.filename = filename;
        this.locname = locname;
        this.color = color;
    }
    public String getFileName() {
        return this.filename;
    }
    public String getlocName() {
        return this.locname;
    }
    public Color getColor() {
        return this.color;
    }
}

public class World_State {
    public int step;
    public int superStep;
    public List<Integer> counts;
    public List<Color> owners; //made it colors for convienience.
    public World_State(World w, int step, int superStep) {
        this.step = step;
        this.superStep = superStep;
        this.counts = new ArrayList<>();
        this.owners = new ArrayList<>();
        for (int i = 0; i < w.nodes.size(); i++) {
            World.Node node = w.nodes.get(i);
            this.counts.add(node.getSoldiers());
            String anonOwner = node.getOwner();
            Color c;
            if (anonOwner.equals("N")) {
                c = Color.GRAY;
            } else {
                //System.out.println();
                //System.out.println(anonOwner);
                //System.out.println(w.anonToName.get(anonOwner));
                //System.out.println(agentLookup.get(w.anonToName.get(anonOwner)));
                //System.out.println();
            c = agentLookup.get(w.anonToName.get(anonOwner)).getColor();
            }
            this.owners.add(c);
        }
    }
}

public static void main(String args[]) {   
  
    //Simulation sim = new Simulation(15);
    //sim.run(20);       
    }
}
