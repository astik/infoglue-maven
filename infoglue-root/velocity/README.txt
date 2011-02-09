Sources from http://svn.apache.org/repos/asf/velocity/engine/tags/1.7

Class changed : org.apache.velocity.runtime.parser.node.ASTReference


From http://www.infoglue.org/forum/posts/list/30/126.page#546

When it comes to velocity it's only a small method in one class I have patched: 
The old one was 
    private String getNullString(InternalContextAdapter context)
    {
        Object callingArgument = context.get(".literal." + nullString);

        if (callingArgument != null)
            return ((Node) callingArgument).literal();
        else
            return nullString;
    }
The exception we got was that when we had a null or nonexistent value velocity (unlike in 1.5) throws an exception here because the callingArgument is not a Node in many of our cases but rather our Action-object.
I have not checked deeper into why this is and if there are a better way to solve it but I changed the method into: 
    private String getNullString(InternalContextAdapter context)
    {
    	Object callingArgument = context.get(".literal." + nullString);

        if (callingArgument != null)
        {
        	if(callingArgument instanceof Node)
        	{
        		return ((Node) callingArgument).literal();
        	}
        	else
        	{
        		System.out.println("callingArgument was not an instance of Node: [" + this.literal() + "] in "  + callingArgument.getClass().getName());
        		log.error("callingArgument was not an instance of Node: [" + this.literal() + "] in "  + callingArgument.getClass().getName());
        		return nullString;
        	}
        }
        else
            return nullString;
    }
This can be used by us to find all nullplaces as the system out in it writes out the location.
Perhaps we can fix most problems in the velocity files but the risk is imminent that there are special cases that may be missed.
And having velocity break down in that case seems bad. Instead I propose we keep our patched jar and changed class and try to talk to the velocity guys.
We should fix as many of the velocity-problems we can also. Hopefully a future version of velocity will have corrected this and then we can throw away ours. 
   