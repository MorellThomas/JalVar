import jalview.datamodel.SequenceFeature
import jalview.gui.Desktop
def af = jalview.bin.Jalview.currentAlignFrame
def av = af.viewport
def fr = Desktop.getAlignFrameFor(av.codingComplement).getFeatureRenderer()
def counts = 0
def countm = 0
for (seq in av.alignment.sequences) 
{
   ds = seq.datasetSequence
   for (res = ds.start ; res <= ds.end; res++) 
   {
     mf = fr.findComplementFeaturesAtResidue(seq, res)
     if (mf != null)
     {
         for (feature in mf.features)
         {
           variant = mf.findProteinVariants(feature)
           if (!"".equals(variant))
           {
               type = variant.contains("=") ? "synonymous_variant" : "missense_variant"
               if (type.equals("synonymous_variant")) counts++ else countm++;
               sf = new SequenceFeature(type, variant, res, res, null)
               seq.addSequenceFeature(sf)
           }
         }
     }
   }
}
af.getFeatureRenderer().featuresAdded()
af.alignPanel.paintAlignment(true, true)
println "Added " + countm + " missense and " + counts + " synonymous variants"