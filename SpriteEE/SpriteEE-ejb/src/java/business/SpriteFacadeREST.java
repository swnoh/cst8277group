/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import business.Sprite;
import business.SyncRequest;
import business.SyncResult;
import business.AbstractFacade;
import data.VersionNotMatchException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author tgk
 */
@Stateless
@Path("business.sprite")
public class SpriteFacadeREST extends AbstractFacade<Sprite> {
    @PersistenceContext(name = "SpriteEE-ejbPU")
    private EntityManager em;
    private static final int MAX_NUM_CONTACT_TO_SYNCH = 1000;
    public SpriteFacadeREST() {
        super(Sprite.class);
    }

    @POST
    @Override
    @Consumes({"application/xml", "application/json"})
    public void create(Sprite entity) {
        System.out.println("called create");
        entity.setUpdateTime(System.currentTimeMillis());
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") String id, Sprite entity) {
        System.out.println("called edit");
        entity.setUpdateTime(System.currentTimeMillis());
        super.edit(entity);
    }

    @Override
    public void edit(Sprite s){
        s.setUpdateTime(System.currentTimeMillis());
        super.edit(s);
    }
    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        System.out.println("called remove(id)");
        Sprite s = super.find(id);
        s.setDeleted(true);
        edit(s);
    }
    
    @Override public void remove(Sprite s){
        System.out.println("called remove(Sprite)");
        s.setDeleted(true);
        edit(s);
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Sprite find(@PathParam("id") String id) {
        System.out.println("called find(String)");
        return super.find(id);
    }

    @GET
    @Override
    @Produces({"application/xml", "application/json"})
    public List<Sprite> findAll() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<Sprite> rt = cq.from(Sprite.class);
        cq.where(cb.equal(rt.get("deleted"), false));
        return getEntityManager().createQuery(cq).getResultList();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<Sprite> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        System.out.println("I am running findRange");
        return findRange(new int[]{from, to});
    }

    @Override
    public List<Sprite> findRange(int[] range){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<Sprite> rt = cq.from(Sprite.class);
        cq.where(cb.equal(rt.get("deleted"), false));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }
    
    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST() {
        return String.valueOf(count());
    }

    @Override
    public int count() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        javax.persistence.criteria.CriteriaQuery cq = cb.createQuery();
        javax.persistence.criteria.Root<Sprite> rt = cq.from(Sprite.class);
        cq.where(cb.equal(rt.get("deleted"), false));
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    @POST
    @Path("sync")
    @Produces({"application/xml", "application/json"})
    @Consumes({"application/xml", "application/json"})
    public SyncResult syncREST(business.SyncRequest syncRequest) {
        SyncResult result = null;
        try {
            result =  sync(syncRequest);
        } catch (IOException ex) {
            Logger.getLogger(SpriteFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    public SyncResult sync(SyncRequest syncR) throws IOException {
		try {
			List<Sprite> modified = syncR.getModified();
			 /** 
			  * The now time will be used for next sync.
			  * It is possible that another client can modified the data after the "now" timestamp before the call to findChanged. 
			  * In this case the modified data is already included in the changedData set, however
			  * the next sync, the modified data will be included in the changedData set again since 
			  * its update time is great than "now"
			  *                                                   
			  */
			Long now = new Long (System.currentTimeMillis());
			List<Sprite> conflict = new ArrayList<Sprite>();
			/***
			 * For now, we only support the number records changes on the server side to max 1000
			 */
			List<Sprite> serverSideChangedData = findChanged(syncR.getSyncTime(), 0, MAX_NUM_CONTACT_TO_SYNCH);
			SyncResult ret = new SyncResult(serverSideChangedData, conflict, now);
	
			for (Sprite c : modified) {
				try {
					storeOrUpdateSprite(c);
				} catch (VersionNotMatchException e) {
					/***
					 * The client's version does not match the server's version
					 * The server must changed its data since last sync
					 */
					Sprite conflictContact = findContact(
							serverSideChangedData, c);
					if (conflictContact != null) {
						/***
						 * confirms that server did change the data since last
						 * sync.
						 */
						conflict.add(conflictContact);
					} else {
						conflictContact = find(c.getId());
						if (conflictContact.getVersion() > c.getVersion()) {
							conflict.add(conflictContact);
						} else {
							/***
							 * we got here because the client has a new version
							 * that the server's It must be client is sending
							 * the wrong data Wrong.
							 */
							throw new IllegalArgumentException(
									"Client is sending the wrong version of the data");
						}
					}
				}
			}
			removeContact(serverSideChangedData, modified);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	public Sprite storeOrUpdateSprite(Sprite sprite) throws IOException {
            //    try {
                        sprite.setUpdateTime(System.currentTimeMillis());
                        if( sprite.getId() == null) {
                                sprite.setId(UUID.randomUUID().toString());
                        }
                        if ( sprite.getVersion() == 0 ) {
                                sprite.setVersion(1L);
                        }
                        em.merge(sprite);
//                } catch (HibernateOptimisticLockingFailureException e ) {
//                        throw new VersionNotMatchException("version does not match", e);
//                }
                return sprite;
        }
	
	public List<Sprite> findChanged(long timestamp, int start, int numOfmatches) {
            System.out.println("called findChanged");
            TypedQuery<Sprite> q = 
                    em.createQuery("Select s FROM Sprite s where s.updateTime > " + timestamp, Sprite.class );
            List<Sprite> result = q.getResultList();
            System.out.println("during sync, " + result.size() + " changed since last sync");
		return result;
	}
	
	private void removeContact(List<Sprite> contactList, Sprite contact) {
            System.out.println("called removeContact");
		for ( Sprite c : contactList) {
			if (c.getId().equals(contact.getId())) {
				contactList.remove(c);
				return;
			}
		}		
	}
	
	private void removeContact(List<Sprite> srcList, List<Sprite> removeList) {
		for ( Sprite c : removeList) {
			removeContact(srcList, c);
		}
	}
	private Sprite findContact(List<Sprite> list, Sprite c) {
		for (Sprite contact : list) {
			if ( contact.getId().equals(c.getId())) {
				return contact;
			}
		}
		return null;
	}
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }  
}
