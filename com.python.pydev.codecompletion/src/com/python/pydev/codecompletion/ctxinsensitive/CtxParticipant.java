/*
 * Created on 07/09/2005
 */
package com.python.pydev.codecompletion.ctxinsensitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.editor.actions.PySelection;
import org.python.pydev.editor.codecompletion.CompletionRequest;
import org.python.pydev.editor.codecompletion.IPyCompletionProposal;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant;
import org.python.pydev.ui.ImageCache;

import com.python.pydev.analysis.CtxInsensitiveImportComplProposal;
import com.python.pydev.analysis.additionalinfo.AbstractAdditionalInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.AdditionalProjectInterpreterInfo;
import com.python.pydev.analysis.additionalinfo.IInfo;
import com.python.pydev.codecompletion.CodecompletionPlugin;

public class CtxParticipant implements IPyDevCompletionParticipant{

    public Collection getGlobalCompletions(CompletionRequest request, ICompletionState state) {
        ImageCache imageCache = CodecompletionPlugin.getImageCache();
        Image classWithImport = imageCache.get(CodecompletionPlugin.CLASS_WITH_IMPORT_ICON);
        Image methodWithImport = imageCache.get(CodecompletionPlugin.METHOD_WITH_IMPORT_ICON);
        Image attributeWithImport = imageCache.get(CodecompletionPlugin.ATTR_WITH_IMPORT_ICON);

        PySelection selection = new PySelection(request.doc);
        int lineAvailableForImport = selection.getLineAvailableForImport();
        
        ArrayList<CtxInsensitiveImportComplProposal> completions = new ArrayList<CtxInsensitiveImportComplProposal>();
        String qual = request.qualifier;
        String lowerQual = qual.toLowerCase();
        
        String initialModule = request.nature.resolveModule(request.editorFile);
        if(qual.length() >= 3){ //at least n characters required...
            List<IInfo> tokensStartingWith = AdditionalProjectInterpreterInfo.getTokensStartingWith(qual, request.nature, AbstractAdditionalInterpreterInfo.TOP_LEVEL);
            
            for (IInfo info : tokensStartingWith) {
                //there always must be a declaringModuleName
                String declaringModuleName = info.getDeclaringModuleName();
                if(initialModule != null && declaringModuleName != null){
                    if(initialModule.equals(declaringModuleName)){
                    	continue;
                    }
                }
                boolean hasInit = false;
                if(declaringModuleName.endsWith(".__init__")){
                	declaringModuleName = declaringModuleName.substring(0, declaringModuleName.length()-9);//remove the .__init__
                	hasInit = true;
                }
                
                String rep = info.getName();
                StringBuffer buffer = new StringBuffer();
                buffer.append("from ");
                buffer.append(declaringModuleName);
                buffer.append(" import ");
                buffer.append(rep);
                String realImportRep = buffer.toString();
                
                buffer = new StringBuffer();
                buffer.append(rep );
                buffer.append(" - ");
                buffer.append(declaringModuleName);
                if(hasInit){
                	buffer.append(".__init__");
                }
                String displayString = buffer.toString();

                //get the image
                Image img;
                if(info.getType() == IInfo.CLASS_WITH_IMPORT_TYPE){
                    img = classWithImport; 
                }else if(info.getType() == IInfo.METHOD_WITH_IMPORT_TYPE){
                    img = methodWithImport; 
                }else if(info.getType() == IInfo.ATTRIBUTE_WITH_IMPORT_TYPE){
                    img = attributeWithImport; 
                }else{
                    throw new RuntimeException("Undefined type.");
                }
                
                
                CtxInsensitiveImportComplProposal  proposal = new CtxInsensitiveImportComplProposal (
                        rep,
                        request.documentOffset - request.qlen, 
                        request.qlen, 
                        realImportRep.length(), 
                        img, 
                        displayString, 
                        (IContextInformation)null, 
                        "", 
                        rep.toLowerCase().equals(lowerQual)? IPyCompletionProposal.PRIORITY_LOCALS_1 : IPyCompletionProposal.PRIORITY_GLOBALS,
                        realImportRep,
                        lineAvailableForImport);
                
                completions.add(proposal);
            }
    
        }        
        return completions;
    }


}
