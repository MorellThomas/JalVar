package jalview.datamodel;

import java.util.Collection;

public interface ContactMapHolderI
{
  /**
   * resolve a contact list instance (if any) associated with the annotation row
   * and column position Columns of ContactMap are indexed relative to context
   * object (columns of alignment, positions on sequence relative to
   * sequence.getStart())
   * 
   * @param _aa
   * @param column
   *          - base 0 column index
   * @return
   */
  ContactListI getContactListFor(AlignmentAnnotation _aa, int column);

  AlignmentAnnotation addContactList(ContactMatrixI cm);

  Collection<ContactMatrixI> getContactMaps();

  public ContactMatrixI getContactMatrixFor(AlignmentAnnotation ann);

  void addContactListFor(AlignmentAnnotation annotation, ContactMatrixI cm);

}
