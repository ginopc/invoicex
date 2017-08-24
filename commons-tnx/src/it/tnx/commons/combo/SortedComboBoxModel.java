/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons.combo;

import java.util.Comparator;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;


/*
 *  Custom model to make sure the items are stored in a sorted order.
 *  The default is to sort in the natural order of the item, but a
 *  Comparator can be used to customize the sort order.
 */
public class SortedComboBoxModel extends DefaultComboBoxModel {

    private Comparator comparator;

    /*
     *  Create an empty model that will use the natural sort order of the item
     */
    public SortedComboBoxModel() {
        super();
    }

    /*
     *  Create an empty model that will use the specified Comparator
     */
    public SortedComboBoxModel(Comparator comparator) {
        super();
        this.comparator = comparator;
    }

    /*
     *	Create a model with data and use the nature sort order of the items
     */
    public SortedComboBoxModel(Object items[]) {
        this(items, null);
    }

    /*
     *  Create a model with data and use the specified Comparator
     */
    public SortedComboBoxModel(Object items[], Comparator comparator) {
        this.comparator = comparator;

        for (Object item : items) {
            addElement(item);
        }
    }

    /*
     *	Create a model with data and use the nature sort order of the items
     */
    public SortedComboBoxModel(Vector items) {
        this(items, null);
    }

    /*
     *  Create a model with data and use the specified Comparator
     */
    public SortedComboBoxModel(Vector items, Comparator comparator) {
        this.comparator = comparator;

        for (Object item : items) {
            addElement(item);
        }
    }

    @Override
    public void addElement(Object element) {
        insertElementAt(element, 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertElementAt(Object element, int index) {
        int size = getSize();

        //  Determine where to insert element to keep model in sorted order
        for (index = 0; index < size; index++) {
            if (comparator != null) {
                Object o = getElementAt(index);

                if (comparator.compare(o, element) > 0) {
                    break;
                }
            } else {
                Comparable c = (Comparable) String.valueOf(getElementAt(index)).toLowerCase();

                if (c.compareTo( String.valueOf(element).toLowerCase()) > 0) {
                    break;
                }
            }
        }

        super.insertElementAt(element, index);

        //  Select an element when it is added to the beginning of the model
        if (index == 0 && element != null) {
            setSelectedItem(element);
        }
    }
}
