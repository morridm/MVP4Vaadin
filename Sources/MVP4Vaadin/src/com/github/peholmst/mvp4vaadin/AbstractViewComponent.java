/*
 * Copyright (c) 2011 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.peholmst.mvp4vaadin;

import java.lang.reflect.Constructor;

import javax.annotation.PostConstruct;

import com.github.peholmst.mvp4vaadin.events.DescriptionChangedViewEvent;
import com.github.peholmst.mvp4vaadin.events.DisplayNameChangedViewEvent;
import com.github.peholmst.mvp4vaadin.i18n.AbstractI18NViewComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;

/**
 * This is a sibling to {@link AbstractView} and is intended for view
 * implementations that are themselves Vaadin components. It has been designed
 * to be used together with concrete {@link Presenter} implementations. It
 * delegates all {@link View}-methods to a {@link ViewDelegate}.
 * 
 * @see AbstractI18NViewComponent
 * 
 * @author Petter Holmström
 * @since 1.0
 * 
 * @param <V>
 *            the type of the View.
 * @param <P>
 *            the type of the Presenter.
 */
public abstract class AbstractViewComponent<V extends View, P extends Presenter<V>>
		extends CustomComponent implements ViewDelegateOwner<V, P>, VaadinView {

	private static final long serialVersionUID = 8990003143642848504L;

	private final ViewDelegate<V, P> viewDelegate = new ViewDelegate<V, P>(this);

	private final Class<P> presenterClass;

	private final Class<V> viewClass;

	/**
	 * Creates a new <code>AbstractViewComponent</code>. The presenter must be
	 * specified by either overriding {@link #createPresenter()} or calling
	 * {@link #setPresenter(Presenter)} before the view is initialized.
	 */
	public AbstractViewComponent() {
		this(null, null);
	}

	/**
	 * Creates a new <code>AbstractViewComponent</code>. The presenter- and view
	 * classes will be used to create the presenter. If any of them are
	 * <code>null</code>, the constructor acts just like
	 * {@link #AbstractViewComponent()}.
	 * 
	 * @see #createPresenter()
	 */
	public AbstractViewComponent(Class<P> presenterClass, Class<V> viewClass) {
		this.presenterClass = presenterClass;
		this.viewClass = viewClass;
		setCompositionRoot(createCompositionRoot());
	}

	/**
	 * Creates the composition root that will be passed to
	 * {@link #setCompositionRoot(Component)} upon component creation.
	 */
	protected abstract Component createCompositionRoot();

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the {@link #AbstractViewComponent(Class, Class)} constructor was used
	 * to create the component, this implementation will try to create a new
	 * instance using the {@link Presenter#Presenter(View)} constructor. In all
	 * other cases, an exception will be thrown. Subclasses may override.
	 */
	@Override
	public P createPresenter() {
		if (presenterClass == null) {
			throw new IllegalStateException(
					"No presenterClass set - override createPresenter()");
		}
		if (viewClass == null) {
			throw new IllegalStateException(
					"No viewClass set - override createPresenter()");
		}
		try {
			Constructor<P> constructor = presenterClass
					.getConstructor(viewClass);
			return constructor.newInstance(viewClass.cast(this));
		} catch (Exception e) {
			throw new UnsupportedOperationException(
					"Cannot create a new presenter instance - override createPresenter()",
					e);
		}
	}

	@Override
	public String getDisplayName() {
		return viewDelegate.getDisplayName();
	}

	/**
	 * Sets the display name of this view and fires a
	 * {@link DisplayNameChangedViewEvent}.
	 */
	protected void setDisplayName(String displayName) {
		viewDelegate.setDisplayName(displayName);
	}

	@Override
	public String getViewDescription() {
		return viewDelegate.getViewDescription();
	}

	/**
	 * Sets the description of this view and fires a
	 * {@link DescriptionChangedViewEvent}.
	 */
	protected void setViewDescription(String description) {
		viewDelegate.setViewDescription(description);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Please note that this method has been annotated with the
	 * {@link PostConstruct @PostConstruct} annotation. If the view is created
	 * using a container such as Spring or CDI, this method will be
	 * automatically invoked after the view has been created and all the
	 * dependencies have been injected.
	 */
	@Override
	@PostConstruct
	public void init() {
		viewDelegate.init();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is empty, subclasses may override.
	 */
	@Override
	public void initView() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation is empty, subclasses may override.
	 */
	@Override
	public void finalizeInitialization() {
	}

	@Override
	public boolean isInitialized() {
		return viewDelegate.isInitialized();
	}

	@Override
	public void addListener(ViewListener listener) {
		viewDelegate.addListener(listener);
	}

	@Override
	public void removeListener(ViewListener listener) {
		viewDelegate.removeListener(listener);
	}

	@Override
	public void fireViewEvent(ViewEvent event) {
		viewDelegate.fireViewEvent(event);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the view component itself (<code>this</code>
	 * ).
	 */
	@Override
	public ComponentContainer getViewComponent() {
		return this;
	}

	/**
	 * Returns the presenter of this view, or <code>null</code> if none has been
	 * specified or created yet.
	 */
	public P getPresenter() {
		return viewDelegate.getPresenter();
	}

	/**
	 * Sets the presenter of this view. If the view is already initialized, an
	 * exception will be thrown.
	 */
	public void setPresenter(P presenter) {
		viewDelegate.setPresenter(presenter);
	}
}
