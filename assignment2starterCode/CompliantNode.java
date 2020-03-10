import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

	private double p_graph, p_malicious, p_tXDistribution;
    private boolean[] followees;
    private int numRounds;
    private Set<Transaction> pendingTransactions;
    private boolean[] banned;


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_tXDistribution = p_txDistribution;
        this.numRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;

    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactions = pendingTransactions;
        this.banned = new boolean[followees.length];
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
         Set<Transaction> toSendTransactions = new HashSet<>(pendingTransactions);

        return toSendTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        Candidate[] candidatesArray = new Candidate[candidates.size()];
        candidates.toArray(candidatesArray);

        Set<Integer> senders = new HashSet<>();

        for (int i=0; i<candidatesArray.length; i++) {
        	senders.add(candidatesArray[i].sender);
        }

        for (int i = 0; i < followees.length; i++) {
            if (followees[i] == true && senders.contains(i) == false)
                banned[i] = true;
        }

        for (int i=0; i<candidatesArray.length; i++) {
        	if (banned[candidatesArray[i].sender] == false) {
                pendingTransactions.add(candidatesArray[i].tx);
            }
        }
    }
}