
/* First created by JCasGen Mon Aug 25 18:32:09 IDT 2014 */
package ac.biu.nlp.nlp.ie.onthefly.input.uima;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** 
 * Updated by JCasGen Mon Nov 17 02:12:27 EST 2014
 * @generated */
public class TreeoutDepPrepGenPosWithContext_Type extends Treeout_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TreeoutDepPrepGenPosWithContext_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TreeoutDepPrepGenPosWithContext_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TreeoutDepPrepGenPosWithContext(addr, TreeoutDepPrepGenPosWithContext_Type.this);
  			   TreeoutDepPrepGenPosWithContext_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TreeoutDepPrepGenPosWithContext(addr, TreeoutDepPrepGenPosWithContext_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TreeoutDepPrepGenPosWithContext.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ac.biu.nlp.nlp.ie.onthefly.input.uima.TreeoutDepPrepGenPosWithContext");



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TreeoutDepPrepGenPosWithContext_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    