package com.smartbear.restplugin;

import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

@AForm( name = "Add WSDL from ProgrammableWeb", description = "Imports a WSDL for an API in the ProgrammableWeb Directory" )
public interface AddFromProgrammableWebForm
{
   @AField( description = "Status", type = AField.AFieldType.LABEL )
   public final static String STATUS = "Status";

   @AField( description = "API Category", type = AField.AFieldType.COMPONENT )
   public final static String CATEGORY = "Category";

   @AField( description = "API Name", type = AField.AFieldType.COMPONENT )
   public final static String NAME = "Name";

   @AField( description = "API Description", type = AField.AFieldType.INFORMATION )
   public final static String DESCRIPTION = "Description";

   @AField( description = "WSDL", type = AField.AFieldType.LABEL )
   public final static String WSDL = "WSDL";
}