package com.automic.spec

import com.uc4.communication.Connection;
import com.uc4.api.InvalidUC4NameException
import com.uc4.api.UC4HostName
import com.uc4.api.UC4ObjectName
import com.uc4.api.objects.Job;
import com.uc4.api.objects.UC4Object
import com.uc4.communication.Connection;
import com.automic.objects.CommonAERequests;
import com.automic.objects.AEFolderRequests;
import com.automic.utils.*
import com.uc4.api.Template;

class CLIENTSpecCreate {
	public static def CreateObject(Connection connection, JsonUpdates,boolean Commit){
		
		String NAME = JsonUpdates.name;
		String TITLE = JsonUpdates.title;
		
		if(NAME == null || NAME.equals("")){return CommonJSONRequests.renderErrorAsJSON("template in JSON request body should contain a name")}

	}
}
