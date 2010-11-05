
class SecurityFilters {
	def filters = {
		loginCheck(controller:'*', action:'*') {
			before = {	
				
				log.info params
				String [] controllersWithAuthUserNotRequired = "api,test".split(",");
				String [] actionsWithAuthUserNotRequired = "login,handleLogin,signup,handleSignup,json".split(",");
				String [] actionsWithWarehouseNotRequired = "login,handleLogin,signup,handleSignup,chooseWarehouse,viewLogo,json".split(",");

				// Not sure when this happens								
				if (params.controller == null) {
					redirect(controller: 'auth', action:'login')   
					return true
				}			
				// When a request does not require authentication, we return true
				// FIXME In order to start working on sync use cases, we need to authenticate  	
				else if (Arrays.asList(controllersWithAuthUserNotRequired).contains(controllerName)) {
					return true;
				}
				// When there's no authenticated user in the session and a request requires authentication 
				// we redirect to the auth login page.  targetUri is the URI the user was trying to get to.
				else if(!session.user && !(
					Arrays.asList(actionsWithAuthUserNotRequired).contains(actionName))) {
					redirect(controller: 'auth', action:'login', params: ['targetUri': (request.forwardURI - request.contextPath)])
					return false;
				}
					
				// When a user has an authenticated, we want to check if they have an active account
				if (session?.user && !session?.user?.active) { 
					session.user = null;
					flash.message = "Your account is currently inactive";
					redirect(controller: 'auth', action:'login')
					return false;
				}
				
				// When a user has not selected a warehouse and they are requesting an action that requires one, 
				// we redirect to the choose warehouse page.
				if (!session.warehouse && !(Arrays.asList(actionsWithWarehouseNotRequired).contains(actionName))) {						
					if (session?.warehouseStillNotSelected) { 
						flash.message = "Please choose the warehouse you'd like to manage.";
					}
					session.warehouseStillNotSelected = true;
					redirect(controller: 'dashboard', action: 'chooseWarehouse')
					return false;
				}
				
			}
		}
		/*
		shipmentAccess(controller:'shipment', action:'*') {
			before = {
				def user = session.user;
				log.info "\n\n\nshipmentAccess: " + user;
				render(view: "/errors/accessDenied");
				return false;
				
			}
		}*/

	}
}
