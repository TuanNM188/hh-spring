package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalState;
import com.formos.huub.domain.response.portals.IResponsePortalState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortalStateRepository extends JpaRepository<PortalState, UUID> {


    void deleteAllByPortalId(UUID portalId);

    @Query(value = "select" +
        " ps.id as id," +
        " ps.country_code as countryCode," +
        " ctr.name as countryName, " +
        " json_build_object('geoNameId',st.geo_name_id,'name',st.name,'code',st.code) as state," +
        " city.cities," +
        " zc.zipcodes" +
        " from portal_state ps " +
        " left join  location  st on ps.state_id = st.geo_name_id and st.location_type = 'STATE'" +
        " left join  location  ctr on ctr.code = ps.country_code and ctr.location_type = 'COUNTRY'" +
        " left join (" +
        " select psc.id," +
        " json_agg(json_build_object('geoNameId',ct.geo_name_id,'name',ct.name,'code',ct.code)) as cities from portal_state psc" +
        " inner join  location  ct on psc.cities like concat('%', ct.geo_name_id, '%') and ct.location_type = 'CITY'" +
        " where psc.is_delete is false and ct.geo_name_id != '' group by psc.id) as city on city.id = ps.id" +
        " left join (" +
        " select" +
        " psz.id," +
        " json_agg(json_build_object('geoNameId',zc.geo_name_id,'name',zc.name,'code',zc.code)) as zipcodes" +
        " from portal_state psz" +
        " inner join  location  zc on psz.zipcodes like concat('%',zc.geo_name_id,'%') and zc.location_type = 'ZIPCODE'" +
        " where psz.is_delete is false and zc.geo_name_id != '' group by psz.id) as zc" +
        " on zc.id = ps.id" +
        " where ps.is_delete is false and ps.portal_id = :portalId" +
        " order by ps.priority_order asc", nativeQuery = true)
    List<IResponsePortalState> getAllByPortalId(UUID portalId);
}
