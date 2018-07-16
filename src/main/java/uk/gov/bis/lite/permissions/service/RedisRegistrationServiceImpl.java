package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import uk.gov.bis.lite.common.redis.RedissonCache;
import uk.gov.bis.lite.common.redis.Ttl;
import uk.gov.bis.lite.permissions.service.model.RegistrationResult;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.function.Function;

public class RedisRegistrationServiceImpl implements RegistrationService {

  private static final Function<RegistrationResult, Boolean> CACHE_IF_OK = registrationResult ->
      registrationResult.getStatus() == Status.OK;

  private final RegistrationServiceImpl registrationServiceImpl;
  private final RedissonCache redissonCache;
  private final Ttl getRegistrations;
  private final Ttl getRegistration;

  @Inject
  public RedisRegistrationServiceImpl(RegistrationServiceImpl registrationServiceImpl,
                                      RedissonCache redissonCache,
                                      @Named("getRegistrations") Ttl getRegistrations,
                                      @Named("getRegistration") Ttl getRegistration) {
    this.registrationServiceImpl = registrationServiceImpl;
    this.redissonCache = redissonCache;
    this.getRegistrations = getRegistrations;
    this.getRegistration = getRegistration;
  }

  @Override
  public RegistrationResult getRegistrations(String userId) {
    return redissonCache.get(() -> registrationServiceImpl.getRegistrations(userId),
        CACHE_IF_OK,
        "getRegistrations",
        getRegistrations,
        userId);
  }

  @Override
  public RegistrationResult getRegistration(String userId, String reference) {
    return redissonCache.get(() -> registrationServiceImpl.getRegistration(userId, reference),
        CACHE_IF_OK,
        "getRegistration",
        getRegistration,
        userId, reference);
  }

}
