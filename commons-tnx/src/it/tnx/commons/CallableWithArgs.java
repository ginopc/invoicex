/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

import java.util.concurrent.Callable;

/**
 *
 * @author Marco
 */
public abstract class CallableWithArgs implements Callable {

    Object[] m_args;

    public CallableWithArgs() {
    }

    public Object call(Object... args) throws Exception {
        setArgs(args);
        return call();
    }

    public void setArgs(Object... args) {
        m_args = args;
    }

    public int getArgCount() {
        return m_args == null ? 0 : m_args.length;
    }

    public Object[] getArgs() {
        return m_args;
    }

}
