module Fluent
  module KafkaPluginUtil
    module SSLSettings
      def self.included(klass)
        klass.instance_eval {
          # https://github.com/zendesk/ruby-kafka#encryption-and-authentication-using-ssl
          config_param :ssl_ca_cert, :array, :value_type => :string, :default => nil,
                       :desc => "a PEM encoded CA cert to use with and SSL connection."
          config_param :ssl_client_cert, :string, :default => nil,
                       :desc => "a PEM encoded client cert to use with and SSL connection. Must be used in combination with ssl_client_cert_key."
          config_param :ssl_client_cert_key, :string, :default => nil,
                       :desc => "a PEM encoded client cert key to use with and SSL connection. Must be used in combination with ssl_client_cert."
        }
      end

      def read_ssl_file(path)
        return nil if path.nil?

        if path.is_a?(Array)
          path.map { |fp| File.read(fp) }
        else
          File.read(path)
        end
      end
    end

    module SaslSettings
      def self.included(klass)
        klass.instance_eval {
          config_param :principal, :string, :default => nil,
                       :desc => "a Kerberos principal to use with SASL authentication (GSSAPI)."
          config_param :keytab, :string, :default => nil,
                       :desc => "a filepath to Kerberos keytab. Must be used with principal."
        }
      end
    end
  end
end
