package com.softline.yatest.common.ui.service;

import static com.softline.yatest.common.log.SystemOutputLogger.log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

/**
 * Simple proxy through native reflection mechanism for automaticaly logging and browser open/closing support in UI services
 * Unsafe, not contain any type checking
 * TODO Should be eliminated after Guice or some another AOP solution implemented 
 */
public class UiServiceProxyFactory
{
  @SuppressWarnings( "unchecked" )
  public static <T> T getUiServiceProxy( Class<T> wrappedUiServiceClass, final T wrappedUiService )
  {
    return ( T ) Proxy.newProxyInstance( wrappedUiService.getClass().getClassLoader(),
      new Class[] {wrappedUiServiceClass}, new UiServiceInvocationHandler<T>( wrappedUiService )
      {
      } );
  }

  private static class UiServiceInvocationHandler<T> implements InvocationHandler
  {
    final T wrappedUiService;

    public UiServiceInvocationHandler( T underlying )
    {
      this.wrappedUiService = underlying;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
      StringBuffer beforeMessage = new StringBuffer();
      beforeMessage.append( "------> " );
      beforeMessage.append( method.getName() );
      beforeMessage.append( "(" );
      for( int i = 0; args != null && i < args.length; i++ )
      {
        if( i != 0 )
          beforeMessage.append( ", " );
        beforeMessage.append( args[i] );
      }
      beforeMessage.append( ")" );
      log().info( beforeMessage );
      Object returnValue;
      try
      {
        ( ( UiService ) wrappedUiService ).openBrowser();
        returnValue = method.invoke( wrappedUiService, args );
      }
      catch( Exception e )
      {
        log().info( "----------------EXCEPTION----------------" );
        log().info( e.getCause().getMessage() );
        log().info( "----------------EXCEPTION----------------" );
        throw e;
      }
      finally
      {
        ( ( UiService ) wrappedUiService ).closeBrowser();
      }
      StringBuffer afterMessage = new StringBuffer();
      afterMessage.append( "------------> " );
      if( returnValue != null )
      {
        afterMessage.append( returnValue );
      }
      else
      {
        afterMessage.append( " EXECUTED " );
      }
      log().info( afterMessage );
      return returnValue;
    }
  }
}
