package com.naskar.fluentquery.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class MethodRecordProxy<T> implements MethodInterceptor {
	
	private T proxy;
	private T target;
	private List<Method> methods;
	private MethodRecordProxy<?> parent;
	
	@SuppressWarnings("unchecked")
	public MethodRecordProxy(T target) {
		this.target = target;
		this.methods = new ArrayList<Method>();
		
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());
		enhancer.setCallback(this);
		
		this.proxy = (T) enhancer.create();
	}
	
	private <E> MethodRecordProxy(T target, MethodRecordProxy<E> parent) {
		this(target);
		this.parent = parent;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		
		if(parent != null) {
			parent.methods.add(method);
		} else {
			this.methods.add(method);
		}
		
		Object result = methodProxy.invoke(target, args);
		
		if(result == null && !TypeUtils.isValueType(method.getReturnType())) {
			try {
				Object o = method.getReturnType().newInstance();
				MethodRecordProxy proxyTmp = 
					new MethodRecordProxy(o, parent != null ? parent : this);
				result = proxyTmp.getProxy();
			} catch(Exception e) {
				result = null;
			}
		}
		
		return result;
	}
	
	public Method getCalledMethod() {
		return methods.isEmpty() ? null : methods.get(methods.size()-1);
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public void clear() {
		this.methods.clear();
	}
	
	public T getProxy() {
		return proxy;
	}
	
}
