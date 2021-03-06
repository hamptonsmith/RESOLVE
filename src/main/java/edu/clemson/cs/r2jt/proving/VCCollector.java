package edu.clemson.cs.r2jt.proving;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.InfixExp;
import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.type.ConcType;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.verification.AssertiveCode;

/**
 * <p>A little helper class to take a <code>Collection</code> of
 * <code>AssertiveCode</code>s, as would be provided from the Verifier, and
 * deliver individual VCs, with type information from the 
 * <code>AssertiveCode</code> (i.e., that contained in the "free variables"
 * information) propagated down into the expressions of the VCs.</p>
 * 
 * @author H. Smith
 *
 */
public class VCCollector implements Iterable<VerificationCondition> {

    /**
     * <p>The final list of VCs from which to spawn Iterators as requested.</p>
     * 
     * <p>INVARIANT: <code>myFinalVCs != null</code></p>
     */
    private final List<VerificationCondition> myFinalVCs =
            new LinkedList<VerificationCondition>();

    /**
     * <p>Constructs a new <code>VCCollector</code> which may be iterated over
     * to retrieve the VCs.</p>
     * 
     * @param source A <code>Collection</code> of <code>AssertiveCode</code>,
     *               as would be returned from the Verifier, containing the VCs
     *               we'd like to access.
     */
    public VCCollector(Collection<AssertiveCode> source) {
        int sectionNumber = 0;

        //VCs come in batches that all refer to the same cluster of variables.
        //Cycle through each batch.
        for (AssertiveCode batch : source) {
            addVCsInContext(batch, sectionNumber);
            sectionNumber++;
        }
    }

    /**
     * <p>Each <code>AssertiveCode</code> contains a set of VCs that refer to
     * the same set of free variables.  This method adds each VC to the final 
     * list.</p>
     * 
     * @param batch The set of VCs in context.
     * @param sectionNumber The batch number so that we can mirror the numbering
     *                      used by the Verifier. (Ideally, we should eventually
     *                      embed the name of each VC from the Verifier with its
     *                      name for greater robustness.)
     */
    private void addVCsInContext(final AssertiveCode batch,
            final int sectionNumber) {

        List<InfixExp> vCs = batch.getFinalConfirm().split();

        int vcIndex = 1;

        //Iterate over the VCs in the batch
        VerificationCondition curVC;
        for (InfixExp vC : vCs) {
            curVC =
                    new VerificationCondition(vC.getLeft(), vC.getRight(),
                            sectionNumber + "_" + vcIndex);

            myFinalVCs.add(curVC);

            vcIndex++;
        }
    }

    /**
     * <p>Returns an <code>Iterator</code> over the VCs that were collected from
     * the <code>Collection</code> of <code>AssertiveCode</code> provided to
     * the constructor.</p>
     */
    public Iterator<VerificationCondition> iterator() {
        return myFinalVCs.iterator();
    }
}
