import java.util.ArrayList;

public class TxHandler {
    private UTXOPool currentPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.currentPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<UTXO> usedUtxoHashs = new ArrayList<>();
        double sumInputs = 0;
        double sumOutputs = 0;

        for (int i = 0; i < inputs.size(); i++) {
            Transaction.Input currentInput = inputs.get(i);

            UTXO utxo = new UTXO(currentInput.prevTxHash, currentInput.outputIndex);

            // Check all ouputs are in the current UTXO pool.
            if (!this.currentPool.contains(utxo)) {
                return false;
            }

            // Check the signatures on each inputs.
            if (Crypto.verifySignature(this.currentPool.getTxOutput(utxo).address, tx.getRawDataToSign(i), currentInput.signature) == false) {
                return false;
            }

            sumInputs += this.currentPool.getTxOutput(utxo).value;
        }

        // Check all of txs output values are non-negative.
        for (int i = 0; i < outputs.size(); i++) {
            Transaction.Output currentOutput = outputs.get(i);
            if (currentOutput.value < 0) {
                return false;
            }

            sumOutputs += currentOutput.value;
        }

        // Check the sum of txs input values is greater than or equal to the sum of its output.
        if (sumInputs < sumOutputs) {
            return false;
        }

        // Check no UTXO is claimed multiple times.
        for (int i = 0; i < inputs.size(); i++) {
            Transaction.Input currentInput = inputs.get(i);
            UTXO utxo = new UTXO(currentInput.prevTxHash, currentInput.outputIndex);

            usedUtxoHashs.add(utxo);
        }

        for (int i = 0; i < usedUtxoHashs.size(); i++) {
            UTXO currentUtxo = usedUtxoHashs.get(i);

            for (int j = i+1; j < usedUtxoHashs.size(); j++) {
                if (currentUtxo.equals(usedUtxoHashs.get(j))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();

        for (int i = 0; i < possibleTxs.length; i++) {
            if (this.isValidTx(possibleTxs[i])) {
                validTransactions.add(possibleTxs[i]);
            }
        }

        Transaction[] txArray = new Transaction[validTransactions.size()];

        for (int i = 0; i < txArray.length; i++) {
            txArray[i] = validTransactions.get(i);

            for (Transaction.Input input: txArray[i].getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                this.currentPool.removeUTXO(utxo);
            }

            for (int j = 0; j < txArray[i].numOutputs(); j++) {
                UTXO utxo = new UTXO(txArray[i].getHash(), j);
                this.currentPool.addUTXO(utxo, txArray[i].getOutput(j));
            }
        }

        return txArray;
    }

}
