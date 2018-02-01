//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util;

import java.io.Externalizable;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* ------------------------------------------------------------ */

/** Map implementation Optimized for Strings keys..
 * This String Map has been optimized for mapping small sets of
 * Strings where the most frequently accessed Strings have been put to
 * the map first.
 *
 * It also has the benefit that it can look up entries by substring or
 * sections of char and byte arrays.  This can prevent many String
 * objects from being created just to look up in the map.
 *
 * This map is NOT synchronized.
 */
public class StringMap extends AbstractMap implements Externalizable
{
    public static final boolean CASE_INSENSTIVE=true;
    protected static final int __HASH_WIDTH=17;
    
    /* ------------------------------------------------------------ */
    protected int _width=__HASH_WIDTH;
    protected Nodes _root=new Nodes();
    protected boolean _ignoreCase=false;
    protected NullEntry _nullEntry=null;
    protected Object _nullValue=null;
    protected HashSet _entrySet=new HashSet(3);
    protected Set _umEntrySet=Collections.unmodifiableSet(_entrySet);
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public StringMap()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param ignoreCase 
     */
    public StringMap(boolean ignoreCase)
    {
        this();
        _ignoreCase=ignoreCase;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param ignoreCase 
     * @param width Width of hash tables, larger values are faster but
     * use more memory.
     */
    public StringMap(boolean ignoreCase,int width)
    {
        this();
        _ignoreCase=ignoreCase;
        _width=width;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the ignoreCase attribute.
     * @param ic If true, the map is case insensitive for keys.
     */
    public void setIgnoreCase(boolean ic)
    {
        if (_root._children!=null)
            throw new IllegalStateException("Must be set before first put");
        _ignoreCase=ic;
    }

    /* ------------------------------------------------------------ */
    public boolean isIgnoreCase()
    {
        return _ignoreCase;
    }

    /* ------------------------------------------------------------ */
    /** Set the hash width.
     * @param width Width of hash tables, larger values are faster but
     * use more memory.
     */
    public void setWidth(int width)
    {
        _width=width;
    }

    /* ------------------------------------------------------------ */
    public int getWidth()
    {
        return _width;
    }
    
    /* ------------------------------------------------------------ */
    @Override
    public Object put(Object key, Object value)
    {
        if (key==null)
            return put(null,value);
        return put(key.toString(),value);
    }
        
    /* ------------------------------------------------------------ */
    public Object put(String key, Object value)
    {
        if (key==null)
        {
            Object oldValue=_nullValue;
            _nullValue=value;
            if (_nullEntry==null)
            {   
                _nullEntry=new NullEntry();
                _entrySet.add(_nullEntry);
            }
            return oldValue;
        }
        
        Nodes Nodes = _root;
        int ni=-1;
        Nodes prev = null;
        Nodes parent = null;

        // look for best match
    charLoop:
        for (int i=0;i<key.length();i++)
        {
            char c=key.charAt(i);
            
            // Advance Nodes
            if (ni==-1)
            {
                parent=Nodes;
                prev=null;
                ni=0;
                Nodes=(Nodes._children==null)?null:Nodes._children[c%_width];
            }
            
            // Loop through a Nodes chain at the same level
            while (Nodes!=null) 
            {
                // If it is a matching Nodes, goto next char
                if (Nodes._char[ni]==c || _ignoreCase&&Nodes._ochar[ni]==c)
                {
                    prev=null;
                    ni++;
                    if (ni==Nodes._char.length)
                        ni=-1;
                    continue charLoop;
                }

                // no char match
                // if the first char,
                if (ni==0)
                {
                    // look along the chain for a char match
                    prev=Nodes;
                    Nodes=Nodes._next;
                }
                else
                {
                    // Split the current Nodes!
                    Nodes.split(this,ni);
                    i--;
                    ni=-1;
                    continue charLoop;
                }
            }

            // We have run out of Nodess, so as this is a put, make one
            Nodes = new Nodes(_ignoreCase,key,i);

            if (prev!=null) // add to end of chain
                prev._next=Nodes;
            else if (parent!=null) // add new child
            {
                if (parent._children==null)
                    parent._children=new Nodes[_width];
                parent._children[c%_width]=Nodes;
                int oi=Nodes._ochar[0]%_width;
                if (Nodes._ochar!=null && Nodes._char[0]%_width!=oi)
                {
                    if (parent._children[oi]==null)
                        parent._children[oi]=Nodes;
                    else
                    {
                        Nodes n=parent._children[oi];
                        while(n._next!=null)
                            n=n._next;
                        n._next=Nodes;
                    }
                }
            }
            else // this is the root.
                _root=Nodes;
            break;
        }
        
        // Do we have a Nodes
        if (Nodes!=null)
        {
            // Split it if we are in the middle
            if(ni>0)
                Nodes.split(this,ni);
        
            Object old = Nodes._value;
            Nodes._key=key;
            Nodes._value=value;
            _entrySet.add(Nodes);
            return old;
        }
        return null;
    }
    
    /* ------------------------------------------------------------ */
    @Override
    public Object get(Object key)
    {
        if (key==null)
            return _nullValue;
        if (key instanceof String)
            return get((String)key);
        return get(key.toString());
    }
    
    /* ------------------------------------------------------------ */
    public Object get(String key)
    {
        if (key==null)
            return _nullValue;
        
        Entry entry = getEntry(key,0,key.length());
        if (entry==null)
            return null;
        return entry.getValue();
    }
    
    /* ------------------------------------------------------------ */
    /** Get a map entry by substring key.
     * @param key String containing the key
     * @param offset Offset of the key within the String.
     * @param length The length of the key 
     * @return The Map.Entry for the key or null if the key is not in
     * the map.
     */
    public Entry getEntry(String key,int offset, int length)
    {
        if (key==null)
            return _nullEntry;
        
        Nodes Nodes = _root;
        int ni=-1;

        // look for best match
    charLoop:
        for (int i=0;i<length;i++)
        {
            char c=key.charAt(offset+i);

            // Advance Nodes
            if (ni==-1)
            {
                ni=0;
                Nodes=(Nodes._children==null)?null:Nodes._children[c%_width];
            }
            
            // Look through the Nodes chain
            while (Nodes!=null) 
            {
                // If it is a matching Nodes, goto next char
                if (Nodes._char[ni]==c || _ignoreCase&&Nodes._ochar[ni]==c)
                {
                    ni++;
                    if (ni==Nodes._char.length)
                        ni=-1;
                    continue charLoop;
                }

                // No char match, so if mid Nodes then no match at all.
                if (ni>0) return null;

                // try next in chain
                Nodes=Nodes._next;                
            }
            return null;
        }
        
        if (ni>0) return null;
        if (Nodes!=null && Nodes._key==null)
            return null;
        return Nodes;
    }
    
    /* ------------------------------------------------------------ */
    /** Get a map entry by char array key.
     * @param key char array containing the key
     * @param offset Offset of the key within the array.
     * @param length The length of the key 
     * @return The Map.Entry for the key or null if the key is not in
     * the map.
     */
    public Entry getEntry(char[] key,int offset, int length)
    {
        if (key==null)
            return _nullEntry;
        
        Nodes Nodes = _root;
        int ni=-1;

        // look for best match
    charLoop:
        for (int i=0;i<length;i++)
        {
            char c=key[offset+i];

            // Advance Nodes
            if (ni==-1)
            {
                ni=0;
                Nodes=(Nodes._children==null)?null:Nodes._children[c%_width];
            }
            
            // While we have a Nodes to try
            while (Nodes!=null) 
            {
                // If it is a matching Nodes, goto next char
                if (Nodes._char[ni]==c || _ignoreCase&&Nodes._ochar[ni]==c)
                {
                    ni++;
                    if (ni==Nodes._char.length)
                        ni=-1;
                    continue charLoop;
                }

                // No char match, so if mid Nodes then no match at all.
                if (ni>0) return null;

                // try next in chain
                Nodes=Nodes._next;                
            }
            return null;
        }
        
        if (ni>0) return null;
        if (Nodes!=null && Nodes._key==null)
            return null;
        return Nodes;
    }

    /* ------------------------------------------------------------ */
    /** Get a map entry by byte array key, using as much of the passed key as needed for a match.
     * A simple 8859-1 byte to char mapping is assumed.
     * @param key char array containing the key
     * @param offset Offset of the key within the array.
     * @param maxLength The length of the key 
     * @return The Map.Entry for the key or null if the key is not in
     * the map.
     */
    public Entry getBestEntry(byte[] key,int offset, int maxLength)
    {
        if (key==null)
            return _nullEntry;
        
        Nodes Nodes = _root;
        int ni=-1;

        // look for best match
    charLoop:
        for (int i=0;i<maxLength;i++)
        {
            char c=(char)key[offset+i];

            // Advance Nodes
            if (ni==-1)
            {
                ni=0;
                
                Nodes child = (Nodes._children==null)?null:Nodes._children[c%_width];
                
                if (child==null && i>0)
                    return Nodes; // This is the best match
                Nodes=child;           
            }
            
            // While we have a Nodes to try
            while (Nodes!=null) 
            {
                // If it is a matching Nodes, goto next char
                if (Nodes._char[ni]==c || _ignoreCase&&Nodes._ochar[ni]==c)
                {
                    ni++;
                    if (ni==Nodes._char.length)
                        ni=-1;
                    continue charLoop;
                }

                // No char match, so if mid Nodes then no match at all.
                if (ni>0) return null;

                // try next in chain
                Nodes=Nodes._next;                
            }
            return null;
        }
        
        if (ni>0) return null;
        if (Nodes!=null && Nodes._key==null)
            return null;
        return Nodes;
    }
    
    
    /* ------------------------------------------------------------ */
    @Override
    public Object remove(Object key)
    {
        if (key==null)
            return remove(null);
        return remove(key.toString());
    }
    
    /* ------------------------------------------------------------ */
    public Object remove(String key)
    {
        if (key==null)
        {
            Object oldValue=_nullValue;
            if (_nullEntry!=null)
            {
                _entrySet.remove(_nullEntry);   
                _nullEntry=null;
                _nullValue=null;
            }
            return oldValue;
        }
        
        Nodes Nodes = _root;
        int ni=-1;

        // look for best match
    charLoop:
        for (int i=0;i<key.length();i++)
        {
            char c=key.charAt(i);

            // Advance Nodes
            if (ni==-1)
            {
                ni=0;
                Nodes=(Nodes._children==null)?null:Nodes._children[c%_width];
            }
            
            // While we have a Nodes to try
            while (Nodes!=null) 
            {
                // If it is a matching Nodes, goto next char
                if (Nodes._char[ni]==c || _ignoreCase&&Nodes._ochar[ni]==c)
                {
                    ni++;
                    if (ni==Nodes._char.length)
                        ni=-1;
                    continue charLoop;
                }

                // No char match, so if mid Nodes then no match at all.
                if (ni>0) return null;

                // try next in chain
                Nodes=Nodes._next;         
            }
            return null;
        }

        if (ni>0) return null;
        if (Nodes!=null && Nodes._key==null)
            return null;
        
        Object old = Nodes._value;
        _entrySet.remove(Nodes);
        Nodes._value=null;
        Nodes._key=null;
        
        return old; 
    }

    /* ------------------------------------------------------------ */
    @Override
    public Set entrySet()
    {
        return _umEntrySet;
    }
    
    /* ------------------------------------------------------------ */
    @Override
    public int size()
    {
        return _entrySet.size();
    }

    /* ------------------------------------------------------------ */
    @Override
    public boolean isEmpty()
    {
        return _entrySet.isEmpty();
    }

    /* ------------------------------------------------------------ */
    @Override
    public boolean containsKey(Object key)
    {
        if (key==null)
            return _nullEntry!=null;
        return
            getEntry(key.toString(),0,key==null?0:key.toString().length())!=null;
    }
    
    /* ------------------------------------------------------------ */
    @Override
    public void clear()
    {
        _root=new Nodes();
        _nullEntry=null;
        _nullValue=null;
        _entrySet.clear();
    }

    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class Nodes implements Entry
    {
        char[] _char;
        char[] _ochar;
        Nodes _next;
        Nodes[] _children;
        String _key;
        Object _value;
        
        Nodes(){}
        
        Nodes(boolean ignoreCase,String s, int offset)
        {
            int l=s.length()-offset;
            _char=new char[l];
            _ochar=new char[l];
            for (int i=0;i<l;i++)
            {
                char c=s.charAt(offset+i);
                _char[i]=c;
                if (ignoreCase)
                {
                    char o=c;
                    if (Character.isUpperCase(c))
                        o=Character.toLowerCase(c);
                    else if (Character.isLowerCase(c))
                        o=Character.toUpperCase(c);
                    _ochar[i]=o;
                }
            }
        }

        Nodes split(StringMap map,int offset)
        {
            Nodes split = new Nodes();
            int sl=_char.length-offset;
            
            char[] tmp=this._char;
            this._char=new char[offset];
            split._char = new char[sl];
            System.arraycopy(tmp,0,this._char,0,offset);
            System.arraycopy(tmp,offset,split._char,0,sl);

            if (this._ochar!=null)
            {
                tmp=this._ochar;
                this._ochar=new char[offset];
                split._ochar = new char[sl];
                System.arraycopy(tmp,0,this._ochar,0,offset);
                System.arraycopy(tmp,offset,split._ochar,0,sl);
            }
            
            split._key=this._key;
            split._value=this._value;
            this._key=null;
            this._value=null;
            if (map._entrySet.remove(this))
                map._entrySet.add(split);

            split._children=this._children;            
            this._children=new Nodes[map._width];
            this._children[split._char[0]%map._width]=split;
            if (split._ochar!=null && this._children[split._ochar[0]%map._width]!=split)
                this._children[split._ochar[0]%map._width]=split;

            return split;
        }
        
        public Object getKey(){return _key;}
        public Object getValue(){return _value;}
        public Object setValue(Object o){Object old=_value;_value=o;return old;}
        @Override
        public String toString()
        {
            StringBuilder buf=new StringBuilder();
            toString(buf);
            return buf.toString();
        }

        private void toString(StringBuilder buf)
        {
            buf.append("{[");
            if (_char==null)
                buf.append('-');
            else
                for (int i=0;i<_char.length;i++)
                    buf.append(_char[i]);
            buf.append(':');
            buf.append(_key);
            buf.append('=');
            buf.append(_value);
            buf.append(']');
            if (_children!=null)
            {
                for (int i=0;i<_children.length;i++)
                {
                    buf.append('|');
                    if (_children[i]!=null)
                        _children[i].toString(buf);
                    else
                        buf.append("-");
                }
            }
            buf.append('}');
            if (_next!=null)
            {
                buf.append(",\n");
                _next.toString(buf);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class NullEntry implements Entry
    {
        public Object getKey(){return null;}
        public Object getValue(){return _nullValue;}
        public Object setValue(Object o)
            {Object old=_nullValue;_nullValue=o;return old;}
        @Override
        public String toString(){return "[:null="+_nullValue+"]";}
    }

    /* ------------------------------------------------------------ */
    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException
    {
        HashMap map = new HashMap(this);
        out.writeBoolean(_ignoreCase);
        out.writeObject(map);
    }
    
    /* ------------------------------------------------------------ */
    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException
    {
        boolean ic=in.readBoolean();
        HashMap map = (HashMap)in.readObject();
        setIgnoreCase(ic);
        this.putAll(map);
    }
}
