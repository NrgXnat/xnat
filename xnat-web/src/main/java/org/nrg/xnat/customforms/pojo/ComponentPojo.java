/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.pojo;

import lombok.Getter;
import lombok.Setter;

/*
 * A POJO to represent a component
 */

@Getter
@Setter
public class ComponentPojo {

    private String label;
    private String value;
}
