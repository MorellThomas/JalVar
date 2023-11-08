package jalview.datamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jalview.ws.datamodel.MappableContactMatrixI;

public class ContactMapHolder implements ContactMapHolderI
{

  Map<Object, ContactMatrixI> contactmaps = new HashMap<>();

  @Override
  public Collection<ContactMatrixI> getContactMaps()
  {
    if (contactmaps != null && contactmaps.size() > 0)
    {
      return contactmaps.values();
    }
    return Collections.EMPTY_LIST;
  }

  @Override
  public ContactListI getContactListFor(AlignmentAnnotation _aa, int column)
  {
    ContactMatrixI cm = contactmaps.get(_aa.annotationId);
    if (cm == null)
    {
      return null;
    }
    if (cm instanceof MappableContactMatrixI)
    {
      if (_aa.sequenceRef != null)
      {
        return ((MappableContactMatrixI) cm)
                .getMappableContactList(_aa.sequenceRef, column);
      }
    }
    // TODO: could resolve sequence position to column position here
    // TODO: what about for complexes - where contactMatrix may involve two or
    // more sequences
    return cm.getContactList(column);
  }

  @Override
  public AlignmentAnnotation addContactList(ContactMatrixI cm)
  {

    AlignmentAnnotation aa = new AlignmentAnnotation(cm.getAnnotLabel(),
            cm.getAnnotDescr(), new Annotation[0]);
    aa.graph = AlignmentAnnotation.CONTACT_MAP;
    aa.graphMin = cm.getMin();
    aa.graphMax = cm.getMax();
    aa.editable = false;
    aa.calcId = cm.getType();

    contactmaps.put(aa.annotationId, cm);
    // TODO: contact matrices could be intra or inter - more than one refseq
    // possible!
    if (cm instanceof MappableContactMatrixI)
    {
      aa.setSequenceRef(((MappableContactMatrixI) cm).getReferenceSeq());
    }
    return aa;
  }

  @Override
  public ContactMatrixI getContactMatrixFor(AlignmentAnnotation ann)
  {
    return contactmaps == null ? null : contactmaps.get(ann.annotationId);
  }

  @Override
  public void addContactListFor(AlignmentAnnotation annotation,
          ContactMatrixI cm)
  {
    // update annotation with data from contact map
    annotation.graphMin = cm.getMin();
    annotation.graphMax = cm.getMax();
    annotation.editable = false;
    annotation.graph = AlignmentAnnotation.CONTACT_MAP;
    annotation.calcId = cm.getType();
    if (annotation.label == null || "".equals(annotation.label))
    {
      annotation.label = cm.getAnnotLabel();

    }
    if (annotation.description == null || "".equals(annotation.description))
    {
      annotation.description = cm.getAnnotDescr();
    }
    contactmaps.put(annotation.annotationId, cm);
  }
}
