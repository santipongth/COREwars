import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class World {

    // Nested class Node
    public class Node {
        private int index;
        private Node left;
        private Node right;
        public int soldiers;
        public String owner;

        public Node(int index) {
            this.index = index;
            this.soldiers = 0;
            this.owner = "N";
        }

        public String toString() {
            return "(Loc:" +this.index+", Own: " + this.owner + ", Sols:" + this.soldiers + ")";
        }

        public int getIndex() {
            return index;
        }

        public int getSoldiers() {
            return soldiers;
        }

        public void setSoldiers(int soldiers) {
            this.soldiers = soldiers;
            if (this.soldiers == 0) {
                this.setOwner("N");
            }
        }

        public void addSoldiers(String agent_name1, int additional_soldiers) {
            //at this stage we assume the move is legal
            String agent_name = nameToAnon.get(agent_name1);
            if (this.owner.equals("N") | this.owner.equals(agent_name)) {
                this.owner = agent_name;
                this.setSoldiers(this.soldiers + additional_soldiers);
            } else {
                if (this.soldiers >= additional_soldiers) {
                    this.setSoldiers(this.soldiers - additional_soldiers);
                } else {
                    this.owner = agent_name;
                    this.setSoldiers(additional_soldiers -  this.soldiers);
                }
            }
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public void splitRight(int maxx) {
            if (this.soldiers <= maxx) {
                return;
            } 
            int toRight = this.soldiers - maxx;
            this.soldiers = maxx;
            this.getRight().setSoldiers(toRight);
            this.getRight().setOwner(this.owner);
        }
        public void splitLeft(int maxx) {
            if (this.soldiers <= maxx) {
                return;
            } 
            int toLeft = this.soldiers - maxx;
            this.soldiers = maxx;
            this.getLeft().setSoldiers(toLeft);
            this.getLeft().setOwner(this.owner);
        }

        public int visible_in_range(String agent_name, int range) {
            //In future this must search up to range
            if (agent_name.equals(this.owner)) {return 1;}
            if (range == 1) {
                if (this.left.owner.equals(agent_name) | this.right.owner.equals(agent_name)) {
                    return 1;
                } else {
                    return 0;
                }
            }
            Node myLeft = this.left;
            Node myRight = this.right;
            for (int x = 1; x <= range; x++) {
                if (myLeft.owner.equals(agent_name) | myRight.owner.equals(agent_name)) {
                    return 1;
                }
                myLeft = myLeft.left;
                myRight = myRight.right;
            }
            return 0;
        }
    }

    public record Node_State(int count, String owner) {}

    private Node head;
    public List<Node> nodes;
    public String[] agents;
    public List<String> agent_list;
    public List<String> defeated_agents;
    public HashMap<String, Integer> perspectives;
    public int numNodes;
    Set<String> active_agents;
    public HashMap<String, String> anonToName;
    public HashMap<String, String> nameToAnon;
    public Boolean protectNeutral;
    public int max_soldiers;
    public int visability_range;

    public World(int numNodes, String[] agents1, int max_soldiers, int starting_soldiers, int visability_range) {
        if (numNodes <= 0) {
            throw new IllegalArgumentException("Number of nodes must be greater than zero");
        }
        this.numNodes = numNodes;
        this.protectNeutral = true;
        this.max_soldiers = max_soldiers;
        this.visability_range = visability_range;
        //anonymize names...
        this.anonToName = new HashMap<>();
        this.nameToAnon = new HashMap<>();
        this.agents = this.getAnonNames(agents1);
        this.perspectives = new HashMap<>();
        this.defeated_agents = new ArrayList<>();
        this.agent_list = Arrays.asList(this.agents);
        this.active_agents = new HashSet<>(this.agent_list);
        Node[] nodes_tmp = new Node[numNodes];

        // Create nodes and store them in the array
        for (int i = 0; i < numNodes; i++) {
            nodes_tmp[i] = new Node(i);
        }

        // Link the nodes to form a ring
        for (int i = 0; i < numNodes; i++) {
            nodes_tmp[i].setRight(nodes_tmp[(i + 1) % numNodes]);
            nodes_tmp[i].setLeft(nodes_tmp[(i - 1 + numNodes) % numNodes]);
        }

        // Set the head to the first node
        head = nodes_tmp[0];
        nodes = Arrays.asList(nodes_tmp);

        int divisions = numNodes/agents.length;
        int place = 0;
        //System.out.println(" " + divisions);
        for (int i = 0; i < numNodes - 1; i = i+divisions) {
            //System.out.println(" " + place + "," + i);
            //System.out.println(Arrays.toString(agents));
            nodes.get(i).setOwner(agents[place]);
            nodes.get(i).setSoldiers(starting_soldiers);
            perspectives.put(agents[place],i);
            place = place + 1;
            if (place >= agents.length) {break;}
        }
        //nodes.get(1).setOwner("b"); //remove later\
        //nodes.get(1).setSoldiers(25); 
    }

    public String getAnonName(String name) {
        return nameToAnon.get(name);
    }

    public String[] getAnonNames(String[] names) {
        String[] alph = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o"};
        String[] anonedNames = new String[names.length];
        List<String> namelist = Arrays.asList(names);
        int i = 0;
        for (String name : namelist) {
            String anName = alph[i];
            anonToName.put(anName,name);
            nameToAnon.put(name,anName);
            anonedNames[i] = anName;
            i++;
        }
        return anonedNames;
    }

    public int fight_right(Node node) {
        //returns the number of nodes to skip forward
        Node rnode = node.getRight();
        if (rnode.getRight().getOwner().equals(node.owner)) {
            //edge case with 3 tile battle...
            Node rrnode = rnode.getRight();
            if (rnode.getSoldiers() > node.getSoldiers() + rrnode.getSoldiers()) {
                node.setOwner("N");
                rrnode.setOwner("N");
                rnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers() + rrnode.getSoldiers());
                rrnode.setSoldiers(0);
                node.setSoldiers(0);
                rnode.splitLeft(max_soldiers);
                return 1;
            } else if (rnode.getSoldiers() < node.getSoldiers() + rrnode.getSoldiers()) {
                rnode.setOwner("N");
                node.setOwner("N");
                rrnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers() + rrnode.getSoldiers());
                node.setSoldiers(0);
                rnode.setSoldiers(0);
                rrnode.splitLeft(max_soldiers);
                return 2;
            } else if (rnode.getSoldiers() == max_soldiers & (rnode.getSoldiers() == node.getSoldiers() + rrnode.getSoldiers())) {
                node.setSoldiers(0);
                rnode.setSoldiers(0);
                rrnode.setSoldiers(0);
            }
            return 0;
        } else {
            if (rnode.getSoldiers() > node.getSoldiers()) {
                node.setOwner(rnode.getOwner());
                node.setSoldiers(rnode.getSoldiers() + node.getSoldiers());
                rnode.setSoldiers(0);
                node.splitRight(max_soldiers);
                return 1;
            }
            if (rnode.getSoldiers() < node.getSoldiers()) {
                rnode.setOwner(node.getOwner());
                rnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers());
                node.setSoldiers(0);
                node.setOwner("N");
                rnode.splitLeft(max_soldiers);
                return 1;
            } else if (rnode.getSoldiers() == max_soldiers & (rnode.getSoldiers() == node.getSoldiers())) {
                node.setSoldiers(0);
                rnode.setSoldiers(0);
            }
        }

        return 0;
    }

    public int fight_left(Node node) {
        //returns the number of nodes to skip forward
        Node rnode = node.getLeft();
        if (rnode.getLeft().getOwner().equals(node.owner)) {
            //edge case with 3 tile battle...
            Node rrnode = rnode.getLeft();
            if (rnode.getSoldiers() > node.getSoldiers() + rrnode.getSoldiers()) {
                node.setOwner("N");
                rrnode.setOwner("N");
                rnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers() + rrnode.getSoldiers());
                rrnode.setSoldiers(0);
                node.setSoldiers(0);
                rnode.splitRight(max_soldiers);
                return -1;
            } else if (rnode.getSoldiers() < node.getSoldiers() + rrnode.getSoldiers()) {
                rnode.setOwner("N");
                node.setOwner("N");
                rrnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers() + rrnode.getSoldiers());
                node.setSoldiers(0);
                rnode.setSoldiers(0);
                rrnode.splitRight(max_soldiers);
                return -2;
            } else if (rnode.getSoldiers() == max_soldiers & (rnode.getSoldiers() == node.getSoldiers() + rrnode.getSoldiers())) {
                node.setSoldiers(0);
                rnode.setSoldiers(0);
                rrnode.setSoldiers(0);
            }
            return 0;
        } else {
            if (rnode.getSoldiers() > node.getSoldiers()) {
                node.setOwner(rnode.getOwner());
                node.setSoldiers(rnode.getSoldiers() + node.getSoldiers());
                rnode.setSoldiers(0);
                node.splitLeft(max_soldiers);
                return -1;
            }
            if (rnode.getSoldiers() < node.getSoldiers()) {
                rnode.setOwner(node.getOwner());
                rnode.setSoldiers(rnode.getSoldiers() + node.getSoldiers());
                node.setSoldiers(0);
                node.setOwner("N");
                rnode.splitRight(max_soldiers);
                return -1;
            } else if (rnode.getSoldiers() == max_soldiers & (rnode.getSoldiers() == node.getSoldiers())) {
                node.setSoldiers(0);
                rnode.setSoldiers(0);
            }
        }

        return 0;
    }

    public Set<String> resolve(String agent_name1, int right) {
        String agent_name = getAnonName(agent_name1);
        int c = perspectives.get(agent_name);
        if (right == 1) { //right == 1
            for (int i = c; i < c + numNodes + 1; i++) {
                //System.out.println(" " + place + "," + i);
                Node node  = nodes.get(i%numNodes);
                if (node.getSoldiers() > max_soldiers) {
                    node.setSoldiers(max_soldiers);
                }
                if (node.getOwner().equals(node.getRight().getOwner()) | node.getOwner().equals("N") | (this.protectNeutral & node.getRight().getOwner().equals("N"))) {
                    continue;
                } else {
                    int skip = this.fight_right(node);
                    i = i + skip;
                }
                
            }
        } else {
            for (int i = c; i > -c-1; i--) {
                //System.out.println(" " + place + "," + i);
                Node node  = nodes.get((numNodes+i)%numNodes);
                if (node.getSoldiers() > max_soldiers) {
                    node.setSoldiers(max_soldiers);
                }
                if (node.getOwner().equals(node.getLeft().getOwner()) | node.getOwner().equals("N") | (this.protectNeutral & node.getLeft().getOwner().equals("N"))) {
                    continue;
                } else {
                    int skip = this.fight_left(node);
                    i = i + skip;
                }
                
            }
        }

        return this.get_losers();
    }

    public Set<String> get_losers() {
        //Returns agents who are NEWLY defeated.
        Set<String> surviving_agents = new HashSet<>();
        for (Node node : this.nodes) {
            surviving_agents.add(node.getOwner());
        }
        //System.out.println("DEBUG CURRENT ACTIVE: ");
        //System.out.println(Arrays.toString(active_agents.toArray()));
        Set<String> defeated_agents = active_agents.stream().filter(element -> !surviving_agents.contains(element)).collect(Collectors.toSet());
        active_agents = surviving_agents;
        //System.out.println("DEBUG NEW ACTIVE: ");
        //System.out.println(Arrays.toString(active_agents.toArray()));
        //System.out.println("CURRENT DEFEATED:");
        //System.out.println(Arrays.toString(defeated_agents.toArray()));
        //defeated_agents.forEach( (car) -> anonToName.get(car) ); //this didn't work
        Set<String> losers = defeated_agents.stream()
                                 .map(name -> anonToName.get(name)) // Adds "_suffix" to each string
                                 .collect(Collectors.toSet());
        //System.out.println("NEW DEFEATED to RETURN:");
        //System.out.println(Arrays.toString(losers.toArray()));
        return losers;
    }

    public int get_global_perspective(int ploc, String agent_name1) {
        //returns the global location for a ploc in the given agent's perspective.
        String agent_name = getAnonName(agent_name1);
        int shift = this.perspectives.get(agent_name);
        return (ploc + shift)%this.numNodes;
    }

    public List<Node_State> get_perspective(String agent_name1) {
        String agent_name = getAnonName(agent_name1);
        List<Integer> vis = this.get_perspective_map(agent_name, visability_range);
        int c = perspectives.get(agent_name);
        List<Node_State> results = new ArrayList<>();
        for (int i : vis) {
            if (i == 1) {
                if (!this.nodes.get(c%this.numNodes).owner.equals(agent_name)) {
                Node_State ns = new Node_State(this.nodes.get(c%this.numNodes).soldiers, this.nodes.get(c%this.numNodes).owner);
                results.add(ns);
                } else {
                    Node_State ns = new Node_State(this.nodes.get(c%this.numNodes).soldiers, "Y");
                results.add(ns);
                }
            } else {
                results.add(new Node_State(-1, "U"));
            }
            c = c + 1;
        }
        return results;
    }

    public List<Integer> get_perspective_map(String agent_name) {
        //Default to showing only neighbors of owned provinces
        //Should show a list of 1s and 0s which is just visibility..
        //String agent_name = getAnonName(agent_name1);
        int startIndex = perspectives.get(agent_name);
        List<Integer> result = new ArrayList<>();
        int n = nodes.size();

        Predicate<Node> filter = node -> node.visible_in_range(agent_name, 1) == 1; 

        // Iterate from startIndex to end of the list
        for (int i = startIndex; i < n; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }

        // Iterate from start of the list to startIndex
        for (int i = 0; i < startIndex; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }
        return result;
    }

    public List<Integer> get_perspective_map(String agent_name, int visR) {
        Predicate<Node> filter = node -> node.visible_in_range(agent_name, visR) == 1; 
        //String agent_name = getAnonName(agent_name1);
        //Uses the user defined filter
        int startIndex = perspectives.get(agent_name);
        List<Integer> result = new ArrayList<>();
        int n = nodes.size();

        // Iterate from startIndex to end of the list
        for (int i = startIndex; i < n; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }

        // Iterate from start of the list to startIndex
        for (int i = 0; i < startIndex; i++) {
            Node obj = nodes.get(i);
            if (filter.test(obj)) {
                result.add(1);
            } else {result.add(0);}
        }
        return result;
    }

    public void printNodes() {
        if (head == null) {
            System.out.println("Empty world");
            return;
        }

        Node current = head;
        do {
            System.out.print(current.getIndex() + ": " + current.getOwner() + ", " + current.getSoldiers() + "  ");
            current = current.getRight();
        } while (current != head);
        System.out.println();
    }

    public Node getNode(int index) {
        if (index < 0 || index >= nodes.size()) {
            throw new IndexOutOfBoundsException("Index out of range");
        }
        return nodes.get(index);
    }

    public static void main(String[] args) {
        int numNodes = 5; // Example number of nodes
        World world = new World(numNodes, new String[] {"a","b"}, 1000, 75, 1);
        world.printNodes();

        // Access a node by index
        int index = 2;
        Node node = world.getNode(index);
        System.out.println("Node at index " + index + ": " + node.getIndex());
    }
}
